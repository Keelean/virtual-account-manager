package com.keelean.accountmanager.service;


import com.keelean.accountmanager.aspect.ActivityLog;
import com.keelean.accountmanager.dto.AccountCustomerResponseDto;
import com.keelean.accountmanager.dto.AccountUpdateRequestDto;
import com.keelean.accountmanager.dto.FullAccountResponseDto;
import com.keelean.accountmanager.dto.PageableResponse;
import com.keelean.accountmanager.dto.PartnerConfigResponse;
import com.keelean.accountmanager.entity.AccountActivity;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.AccountStatus;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.repo.AccountActivityRepo;
import com.keelean.accountmanager.repo.AccountCustomerRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AccountCustomerService {

    @Autowired
    private AccountCustomerRepo repo;
    @Autowired
    private PartnerAccountConfigService virtualAccountPartnerConfigService;
    private static final Long ACCOUNT_REFRESH_LENGTH = 1L;
    private Long COOL_DOWN_PERIOD = 90L;

    @Autowired
    private AccountActivityRepo accountActivityRepo;

    @SneakyThrows
    public FullAccountResponseDto update(AccountUpdateRequestDto request, String accountId, String partnerId){

        AccountCustomer customer = repo.getVirtualAccountFromList(accountId);
        final AccountCustomer vc = customer;

       if(customer == null){
           log.info("Account ID({}) does not exist!", accountId);
           throw new RestServiceException(ErrorCodes.ACCOUNT_DOES_NOT_EXIST.getCode(), accountId);
       }

       if(!partnerId.equals(customer.getPartnerId())){
           throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), accountId);
       }

        if(request.getAmount().compareTo(BigDecimal.ZERO) > 0){
            customer.getMeta().setAmount(request.getAmount());
        }

        PartnerConfigResponse partnerConfigResponses = virtualAccountPartnerConfigService.findByPartnerId(customer.getPartnerId(), vc.getMeta().getAccountType());

        if(customer.getMeta().getAccountType() == AccountType.DYNAMIC && customer.getMeta().getWaitStartTime() == null){
            //Integer expire = request.getTimeoutInMins();
            customer.getMeta().setWaitStartTime(LocalDateTime.now().plus(1440, ChronoUnit.HOURS));
            validatePartnerAndCustomerName(request.getAccountName(), partnerConfigResponses.getPartnerName());
            customer.getMeta().setAccountName(request.getAccountName());
            customer.getMeta().setAmount(request.getAmount());
            customer.setStatus(AccountStatus.CREATED);
        }else{
            //customer.setExpiryDate(LocalDateTime.now().plus(request.getTimeoutInMins(), ChronoUnit.YEARS));
            customer.getMeta().setAmount(request.getAmount());
            validatePartnerAndCustomerName(request.getAccountName(), partnerConfigResponses.getPartnerName());
            customer.getMeta().setAccountName(request.getAccountName());
            if(customer.getMode() != null && (customer.getMode() == AccountMode.STATIC_INVOICE_CLOSED || customer.getMode() == AccountMode.STATIC_INVOICED_EXTENDED)){
                customer.getMeta().setInvoicePaymentRef(request.getInvoiceRef());
            }
        }
        customer = repo.save(customer);


        return FullAccountResponseDto.builder()
               .referenceId(customer.getReferenceId())
               .accountId(customer.getAccountId())
               .expiryDate(customer.getExpiryDate())
               .build();
    }

    @ActivityLog
    @SneakyThrows
    public AccountCustomerResponseDto findVirtualAccount(String referenceIdOrAccountId){
        AccountCustomer customer = getVirtualAccountFromList2(referenceIdOrAccountId);

        log.info("VirtualAccountCustomer::[{}]",customer);

        if(customer != null){
            String partnerId = customer.getPartnerId();
            List<PartnerAccountConfig> partnerConfigs = virtualAccountPartnerConfigService.findPartnerConfigByIdentifier(partnerId);
            Optional<PartnerAccountConfig> optionalPartnerConfig = partnerConfigs.stream().filter(c -> c.getMeta().getAccountType() == customer.getMeta().getAccountType()).findFirst();

            if(!optionalPartnerConfig.isPresent()){
                log.info("Partner config({}) does not exist! Contact admin", partnerId);
                throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), partnerId);
            }

            PartnerAccountConfig partnerConfig = optionalPartnerConfig.get();

            String accountName;
            String status;
            if(customer.getMeta().getAccountName() == null){
                accountName = partnerConfig.getCode();
            }else {
                accountName = customer.getMeta().getAccountName();
            }
            status = customer.getStatus().name();

            if(LocalDateTime.now().compareTo(customer.getExpiryDate()) >= 0){
                status = AccountStatus.EXPIRED.name();
            }

            if(customer.getMeta().getAccountType() == AccountType.DYNAMIC && customer.getStatus() == AccountStatus.COMPLETED_UNUSABLE){
                status = AccountStatus.COMPLETED_UNUSABLE.name();
            }

            Optional<AccountActivity> accountActivity = accountActivityRepo.findByAccountId(referenceIdOrAccountId);
            if(accountActivity.isEmpty()){
                status = AccountStatus.CREATED.name();
            }

            if(customer.getMeta().getAccountType() == AccountType.DYNAMIC &&  customer.getMeta().getWaitStartTime() != null
                    && LocalDateTime.now().compareTo(customer.getMeta().getWaitStartTime()) >= 0){
                status = AccountStatus.EXPIRED.name();
            }

            return AccountCustomerResponseDto.builder()
                    .accountId(customer.getAccountId())
                    .referenceId(customer.getReferenceId())
                    .expiryDate(customer.getExpiryDate())
                    .accountName(accountName)
                    .amount(customer.getMeta().getAmount())
                    .nickName(partnerConfig.getMeta().getAccountDetails())
                    .status(status)
                    .partnerCode(partnerConfig.getCode())
                    .build();
        }
        return AccountCustomerResponseDto.builder()
                .build();

    }

    public AccountCustomerResponseDto findVirtualAccountAndValidateAmount(String referenceIdOrAccountId, BigDecimal amount){

        AccountCustomerResponseDto vacr = findVirtualAccount(referenceIdOrAccountId);

        AccountCustomer vc = getVirtualAccountFromList2(referenceIdOrAccountId);

        //log.info("VirtualAccountCustomerL::{}", vacr);
        //log.info("VirtualAccountCustomerR::{}", vc);

        if(vc.getMeta().getAccountType() == AccountType.DYNAMIC && vc.getMeta().getWaitStartTime() == null){
            vacr.setStatus(AccountStatus.EXPIRED.name());
            return vacr;
        }

        List<PartnerConfigResponse> partnerConfigResponses = virtualAccountPartnerConfigService.findByPartnerId(vc.getPartnerId());

        Optional<PartnerConfigResponse> partnerConfigResponseOptional = partnerConfigResponses.stream().filter(c -> c.getMode().equals(vc.getMeta().getAccountType().name())).findFirst();

        PartnerConfigResponse configResponse = partnerConfigResponseOptional.orElseGet(() -> null);

        //log.info("PartnerConfigResponse::{}", configResponse);

        if(configResponse == null){
            vacr.setStatus(AccountStatus.EXPIRED.name());
            return vacr;
        }

        log.info("vacr::[{}]", vacr);

        if(configResponse.isExactPayment()){
            if(amount.compareTo(vc.getMeta().getAmount()) != 0){
                vacr.setStatus(AccountStatus.EXPIRED.name());
                return vacr;
            }
        }else {
            log.info("configResponse.isExactPayment()::{}",amount);
            log.info("vacr::2[{}]", vacr);
            if(vc.getMeta().getMinMultiplier() != null && vc.getMeta().getMaxMultiplier() != null){
                log.info("configResponse.isExactPayment()2::{}",amount);
                 vacr = checkAmountAgainstMultipliers(amount, vc.getMeta().getMinMultiplier(),vc.getMeta().getMaxMultiplier(), vacr);
                if (vacr != null) return vacr;
            }else{
                log.info("configResponse.isExactPayment()3::{}",amount);
                vacr = checkAmountAgainstMultipliers(amount, configResponse.getMinDeposit(),configResponse.getMaxDeposit(), vacr);
                if (vacr != null) return vacr;
            }

        }

        return vacr;
    }

    private AccountCustomerResponseDto checkAmountAgainstMultipliers(BigDecimal amount, Integer minMultiplier, Integer maxMultiplier, AccountCustomerResponseDto vacr) {
        if(amount.compareTo(vacr.getAmount().multiply(BigDecimal.valueOf(minMultiplier).divide(BigDecimal.TEN))) < 0){
            log.info("MINMULT");
            vacr.setStatus(AccountStatus.EXPIRED.name());
            return vacr;
        }

        if(amount.compareTo(vacr.getAmount().multiply(BigDecimal.valueOf(maxMultiplier).divide(BigDecimal.TEN))) > 0){
            log.info("MAXMULT");
            vacr.setStatus(AccountStatus.EXPIRED.name());
            return vacr;
        }
        return vacr;
    }

    @SneakyThrows
    public AccountCustomerResponseDto refreshAccountId(String referenceIdOrAccountId, String partnerId){
        AccountCustomer customer = repo.getVirtualAccountFromList(referenceIdOrAccountId);
        if(Objects.isNull(customer)){
            log.info("Virtual account({}) does not exist!", referenceIdOrAccountId);
            throw new RestServiceException(ErrorCodes.ACCOUNT_DOES_NOT_EXIST.getCode(), referenceIdOrAccountId);
        }

        if(Objects.isNull(customer.getExpiryDate())){
            log.info("Virtual account({}) configuration is incomplete", referenceIdOrAccountId);
            throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
        }

        if(!partnerId.equals(customer.getPartnerId())){
            throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), partnerId);
        }


        if(LocalDateTime.now().compareTo(customer.getExpiryDate()) < 0){
            customer.setExpiryDate(customer.getExpiryDate().plusYears(ACCOUNT_REFRESH_LENGTH));
            repo.save(customer);
        }

        return AccountCustomerResponseDto.builder()
                .accountId(customer.getAccountId())
                .referenceId(customer.getReferenceId())
                .expiryDate(customer.getExpiryDate())
                .accountName(customer.getMeta().getAccountName())
                .build();
    }

    @SneakyThrows
    public AccountCustomerResponseDto closeVirtualAccount(String referenceIdOrAccountId, String partnerId){
        AccountCustomer customer = repo.getVirtualAccountFromList(referenceIdOrAccountId);
        if(Objects.isNull(customer)){
            log.info("Virtual account({}) does not exist", referenceIdOrAccountId);
            throw new RestServiceException(ErrorCodes.ACCOUNT_DOES_NOT_EXIST.getCode(), referenceIdOrAccountId);
        }

        if(Objects.isNull(customer.getExpiryDate())){
            log.info("Virtual account({}) configuration is incomplete", referenceIdOrAccountId);
            throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
        }

        if(!partnerId.equals(customer.getPartnerId())){
            throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), partnerId);
        }

        customer.setExpiryDate(LocalDateTime.now());
        customer = repo.save(customer);


        return AccountCustomerResponseDto.builder()
                .accountId(customer.getAccountId())
                .referenceId(customer.getReferenceId())
                .expiryDate(customer.getExpiryDate())
                .accountName(customer.getMeta().getAccountName())
                .build();
    }

    private void validatePartnerAndCustomerName(String accountName, String partnerName) {

        if(Objects.isNull(accountName)){
            throw new RestServiceException(ErrorCodes.VALIDATION_FAILED.getCode(), partnerName);
        }

        log.info("partnerName::{}",partnerName);
        log.info("accountName::{}",accountName);
        log.info("accountName.startsWith::{}",accountName.startsWith(partnerName));


        if(!accountName.startsWith(partnerName)){
            log.info("{} must contain {} as a prefix or suffix", accountName, partnerName);
            throw new RestServiceException(ErrorCodes.ACCOUNT_NAME_VALIDATION_FAILED.getCode(), accountName, partnerName);
        }

        if(accountName.length() > 30){
            log.info("{} must contain is more than 30 characters long",accountName);
            throw new RestServiceException(ErrorCodes.ACCOUNT_LENGTH_VALIDATION.getCode(), accountName);
        }
    }

    public Page<AccountCustomerResponseDto> getAll(String partnerId, Pageable pageable){
        Page<AccountCustomer> pageableAccounts =  repo.findByPartnerId(partnerId, pageable);
        PageableResponse<AccountCustomerResponseDto> pageableResponse = new PageableResponse<>();
        List<AccountCustomerResponseDto> response = pageableAccounts.stream()
                .map(c -> AccountCustomerResponseDto.builder()
                        .accountId(c.getAccountId())
                        .accountName(c.getMeta().getAccountName())
                        .expiryDate(c.getExpiryDate())
                        .amount(c.getMeta().getAmount())
                        .referenceId(c.getReferenceId())
                        .build()).collect(Collectors.toList());

        return new PageImpl<AccountCustomerResponseDto>(response, pageable, response.size());
    }


    public AccountCustomerResponseDto getVirtualAccountFromList(String accountOrReferenceId){

        List<AccountCustomer> vcList = repo.findByReferenceIdOrAccountId(accountOrReferenceId, accountOrReferenceId);


        AccountCustomer vc;

        if(vcList.isEmpty()){
            vc = null;
        }else {
            //return expired dynamic account else return null
            Optional<AccountCustomer> optionalVirtualAccountCustomer = vcList.stream().filter(v -> validateDynamicAccount(v)).findFirst();
            vc = optionalVirtualAccountCustomer.orElseGet(()-> AccountCustomer.builder().build());
        }

        String partnerId = vc.getPartnerId();
        PartnerConfigResponse configResponse = PartnerConfigResponse.builder().build();

        if(Objects.isNull(partnerId)){
            List<PartnerConfigResponse> partnerConfigResponses = virtualAccountPartnerConfigService.findByPartnerId(vc.getPartnerId());

            Optional<PartnerConfigResponse> partnerConfigResponseOptional = partnerConfigResponses.stream().filter(c -> c.getMode().equals(vc.getMeta().getAccountType().name())).findFirst();

            configResponse = partnerConfigResponseOptional.orElseGet(() -> PartnerConfigResponse.builder().build());
        }

        return AccountCustomerResponseDto.builder()
                .referenceId(vc.getReferenceId())
                .accountName(vc.getMeta().getAccountName())
                .accountId(vc.getAccountId())
                .expiryDate(vc.getExpiryDate())
                .status(vc.getStatus() == null ? "" : vc.getStatus().name())
                .amount(vc.getMeta().getAmount())
                //.nickName(configResponse.getPartnerName())
                .build();

    }

    public AccountCustomer getVirtualAccountFromList2(String accountOrReferenceId){

        List<AccountCustomer> vcList = repo.findByReferenceIdOrAccountId(accountOrReferenceId, accountOrReferenceId);


        AccountCustomer vc;

        if(vcList.isEmpty()){
            vc = null;
        }else {
            //return expired dynamic account else return null
            Optional<AccountCustomer> optionalVirtualAccountCustomer = vcList.stream().filter(v -> validateDynamicAccount(v)).findFirst();
            vc = optionalVirtualAccountCustomer.orElseGet(()-> AccountCustomer.builder().build());
            log.info("GGG:{}",vc);
        }

        return vc;
    }

    private boolean validateDynamicAccount(AccountCustomer virtualAccountCustomer){
        return true;
    }
}

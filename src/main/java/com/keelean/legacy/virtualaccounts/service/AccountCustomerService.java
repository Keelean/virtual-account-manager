package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.aspect.ActivityLog;
import com.keelean.legacy.customeraccounts.dto.FullVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.dto.PartnerConfigResponse;
import com.keelean.legacy.customeraccounts.dto.VirtualAccountCustomerResponseDto;
import com.keelean.legacy.customeraccounts.dto.VirtualAccountUpdateRequestDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountActivity;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountCustomer;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountPartnerConfig;
import com.keelean.legacy.customeraccounts.enums.AccountMode;
import com.keelean.legacy.customeraccounts.enums.AccountStatus;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.keelean.legacy.customeraccounts.repo.VirtualAccountActivityRepo;
import com.keelean.legacy.customeraccounts.repo.VirtualAccountCustomerRepo;
import com.example.platform.dto.PageableResponse;
import com.example.platform.exception.RestServiceException;
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
public class VirtualAccountCustomerService {

    @Autowired
    private VirtualAccountCustomerRepo repo;
    @Autowired
    private VirtualAccountPartnerConfigService virtualAccountPartnerConfigService;
    private static final Long ACCOUNT_REFRESH_LENGTH = 1L;
    private Long COOL_DOWN_PERIOD = 90L;

    @Autowired
    private VirtualAccountActivityRepo accountActivityRepo;

    @SneakyThrows
    public FullVirtualAccountResponseDto update(VirtualAccountUpdateRequestDto request, String accountId, String partnerId){

        VirtualAccountCustomer customer = repo.getVirtualAccountFromList(accountId);
        final VirtualAccountCustomer vc = customer;

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

        PartnerConfigResponse partnerConfigResponses = virtualAccountPartnerConfigService.findByPartnerId(customer.getPartnerId(), vc.getMeta().getMode());

        if(customer.getMeta().getMode() == VirtualAccountMode.DYNAMIC && customer.getMeta().getWaitStartTime() == null){
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
            if(customer.getMode() != null && customer.getMode() == AccountMode.INVOICE){
                customer.setInvoicePaymentRef(request.getInvoiceRef());
            }
        }
        customer = repo.save(customer);


        return FullVirtualAccountResponseDto.builder()
               .referenceId(customer.getReferenceId())
               .accountId(customer.getAccountId())
               .expiryDate(customer.getExpiryDate())
               .build();
    }

    @ActivityLog
    @SneakyThrows
    public VirtualAccountCustomerResponseDto findVirtualAccount(String referenceIdOrAccountId){
        VirtualAccountCustomer customer = getVirtualAccountFromList2(referenceIdOrAccountId);

        log.info("VirtualAccountCustomer::[{}]",customer);

        if(customer != null){
            String partnerId = customer.getPartnerId();
            List<VirtualAccountPartnerConfig> partnerConfigs = virtualAccountPartnerConfigService.findPartnerConfigByIdentifier(partnerId);
            Optional<VirtualAccountPartnerConfig> optionalPartnerConfig = partnerConfigs.stream().filter(c -> c.getMeta().getMode() == customer.getMeta().getMode()).findFirst();

            if(!optionalPartnerConfig.isPresent()){
                log.info("Partner config({}) does not exist! Contact admin", partnerId);
                throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), partnerId);
            }

            VirtualAccountPartnerConfig partnerConfig = optionalPartnerConfig.get();

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

            if(customer.getMeta().getMode() == VirtualAccountMode.DYNAMIC && customer.getStatus() == AccountStatus.COMPLETED_UNUSABLE){
                status = AccountStatus.COMPLETED_UNUSABLE.name();
            }

            Optional<VirtualAccountActivity> accountActivity = accountActivityRepo.findByAccountId(referenceIdOrAccountId);
            if(accountActivity.isEmpty()){
                status = AccountStatus.CREATED.name();
            }

            if(customer.getMeta().getMode() == VirtualAccountMode.DYNAMIC &&  customer.getMeta().getWaitStartTime() != null
                    && LocalDateTime.now().compareTo(customer.getMeta().getWaitStartTime()) >= 0){
                status = AccountStatus.EXPIRED.name();
            }

            return VirtualAccountCustomerResponseDto.builder()
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
        return VirtualAccountCustomerResponseDto.builder()
                .build();

    }

    public VirtualAccountCustomerResponseDto findVirtualAccountAndValidateAmount(String referenceIdOrAccountId, BigDecimal amount){

        VirtualAccountCustomerResponseDto vacr = findVirtualAccount(referenceIdOrAccountId);

        VirtualAccountCustomer vc = getVirtualAccountFromList2(referenceIdOrAccountId);

        //log.info("VirtualAccountCustomerL::{}", vacr);
        //log.info("VirtualAccountCustomerR::{}", vc);

        if(vc.getMeta().getMode() == VirtualAccountMode.DYNAMIC && vc.getMeta().getWaitStartTime() == null){
            vacr.setStatus(AccountStatus.EXPIRED.name());
            return vacr;
        }

        List<PartnerConfigResponse> partnerConfigResponses = virtualAccountPartnerConfigService.findByPartnerId(vc.getPartnerId());

        Optional<PartnerConfigResponse> partnerConfigResponseOptional = partnerConfigResponses.stream().filter(c -> c.getMode().equals(vc.getMeta().getMode().name())).findFirst();

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

    private VirtualAccountCustomerResponseDto checkAmountAgainstMultipliers(BigDecimal amount, Integer minMultiplier, Integer maxMultiplier, VirtualAccountCustomerResponseDto vacr) {
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
    public VirtualAccountCustomerResponseDto refreshAccountId(String referenceIdOrAccountId, String partnerId){
        VirtualAccountCustomer customer = repo.getVirtualAccountFromList(referenceIdOrAccountId);
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

        return VirtualAccountCustomerResponseDto.builder()
                .accountId(customer.getAccountId())
                .referenceId(customer.getReferenceId())
                .expiryDate(customer.getExpiryDate())
                .accountName(customer.getMeta().getAccountName())
                .build();
    }

    @SneakyThrows
    public VirtualAccountCustomerResponseDto closeVirtualAccount(String referenceIdOrAccountId, String partnerId){
        VirtualAccountCustomer customer = repo.getVirtualAccountFromList(referenceIdOrAccountId);
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


        return VirtualAccountCustomerResponseDto.builder()
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

    public Page<VirtualAccountCustomerResponseDto> getAll(String partnerId, Pageable pageable){
        Page<VirtualAccountCustomer> pageableAccounts =  repo.findByPartnerId(partnerId, pageable);
        PageableResponse<VirtualAccountCustomerResponseDto> pageableResponse = new PageableResponse<>();
        List<VirtualAccountCustomerResponseDto> response = pageableAccounts.stream()
                .map(c -> VirtualAccountCustomerResponseDto.builder()
                        .accountId(c.getAccountId())
                        .accountName(c.getMeta().getAccountName())
                        .expiryDate(c.getExpiryDate())
                        .amount(c.getMeta().getAmount())
                        .referenceId(c.getReferenceId())
                        .build()).collect(Collectors.toList());

        return new PageImpl<VirtualAccountCustomerResponseDto>(response, pageable, response.size());
    }


    public VirtualAccountCustomerResponseDto getVirtualAccountFromList(String accountOrReferenceId){

        List<VirtualAccountCustomer> vcList = repo.findByReferenceIdOrAccountId(accountOrReferenceId, accountOrReferenceId);


        VirtualAccountCustomer vc;

        if(vcList.isEmpty()){
            vc = null;
        }else {
            //return expired dynamic account else return null
            Optional<VirtualAccountCustomer> optionalVirtualAccountCustomer = vcList.stream().filter(v -> validateDynamicAccount(v)).findFirst();
            vc = optionalVirtualAccountCustomer.orElseGet(()-> VirtualAccountCustomer.builder().build());
        }

        String partnerId = vc.getPartnerId();
        PartnerConfigResponse configResponse = PartnerConfigResponse.builder().build();

        if(Objects.isNull(partnerId)){
            List<PartnerConfigResponse> partnerConfigResponses = virtualAccountPartnerConfigService.findByPartnerId(vc.getPartnerId());

            Optional<PartnerConfigResponse> partnerConfigResponseOptional = partnerConfigResponses.stream().filter(c -> c.getMode().equals(vc.getMeta().getMode().name())).findFirst();

            configResponse = partnerConfigResponseOptional.orElseGet(() -> PartnerConfigResponse.builder().build());
        }

        return VirtualAccountCustomerResponseDto.builder()
                .referenceId(vc.getReferenceId())
                .accountName(vc.getMeta().getAccountName())
                .accountId(vc.getAccountId())
                .expiryDate(vc.getExpiryDate())
                .status(vc.getStatus() == null ? "" : vc.getStatus().name())
                .amount(vc.getMeta().getAmount())
                //.nickName(configResponse.getPartnerName())
                .build();

    }

    public VirtualAccountCustomer getVirtualAccountFromList2(String accountOrReferenceId){

        List<VirtualAccountCustomer> vcList = repo.findByReferenceIdOrAccountId(accountOrReferenceId, accountOrReferenceId);


        VirtualAccountCustomer vc;

        if(vcList.isEmpty()){
            vc = null;
        }else {
            //return expired dynamic account else return null
            Optional<VirtualAccountCustomer> optionalVirtualAccountCustomer = vcList.stream().filter(v -> validateDynamicAccount(v)).findFirst();
            vc = optionalVirtualAccountCustomer.orElseGet(()-> VirtualAccountCustomer.builder().build());
            log.info("GGG:{}",vc);
        }

        return vc;
    }

    private boolean validateDynamicAccount(VirtualAccountCustomer virtualAccountCustomer){
        return true;
    }
}

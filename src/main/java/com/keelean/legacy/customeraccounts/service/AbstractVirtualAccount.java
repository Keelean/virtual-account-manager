package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.dto.VirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.entity.*;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.keelean.legacy.customeraccounts.mapper.VirtualAccountCustomerMapper;
import com.keelean.legacy.customeraccounts.repo.VirtualAccountCustomerRepo;
import com.keelean.legacy.customeraccounts.service.impl.VirtualAccountRecursiveTask;
import com.example.platform.dao.EntitySessionManager;
import com.example.platform.exception.RestServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;


@Slf4j
public abstract class AbstractVirtualAccount implements VirtualAccountCreationMode {

    @Autowired
    protected VirtualAccountPoolService accountPoolService;
    @Autowired
    protected VirtualAccountPartnerConfigService configService;
    @Autowired
    protected VirtualAccountCustomerRepo customerRepository;
    @Autowired
    private VirtualAccountCustomerMapper virtualAccountCustomerMapper;

    @Autowired
    private EntitySessionManager entitySessionManager;

    @SneakyThrows
    @Transactional(value = Transactional.TxType.REQUIRED, dontRollbackOn = Throwable.class)
    public VirtualAccount createVirtualAccount(BaseVirtualAccountRequestDto request) {

        log.info("BaseVirtualAccountRequestDto::[{}]", request);

        String referenceId = request.getReferenceId();

        if(Objects.isNull(referenceId) || Strings.isBlank(referenceId)){
            //"Reference ID cannot be empty!
            throw new RestServiceException(ErrorCodes.INVALID_OR_EMPTY_ACCOUNT_ID_OR_REFERENCE.getCode(), referenceId);
        }

        log.info("REFERENCE::{}", referenceId);
        VirtualAccountCustomer customer = customerRepository.getVirtualAccountFromList(referenceId);
        log.info("CUSTOMER::{}", customer);

        if(customer != null){
            log.info("Reference ID already in use!");
            throw new RestServiceException(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), referenceId);
        }

        // Generate new account id if account id is not in the request
        String accountId = request.getAccountId();
        log.info("ACCOUNT ID::{}", accountId);
        VirtualAccount account = null;
        String partnerId = request.getPartnerId();
        if(StringUtils.isBlank(accountId)){
            List<VirtualAccountPartnerConfig> configs = configService.findPartnerConfigByIdentifier(partnerId);

            if(configs.isEmpty()){
                log.info("Partner config does not exist! Contact admin!");
                throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), partnerId);
            }

            Optional<VirtualAccountPartnerConfig> partnerConfigOptional = configs.stream().filter(c -> c.getMeta().getMode() == request.getMode()).findFirst();
            log.info("VirtualAccountPartnerConfig22::{}", partnerConfigOptional);
            if(!partnerConfigOptional.isPresent()){
                log.info("Partner config does not exist! Contact admin!");
                throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), String.format("Partner ID=%s, Account mode=%s", partnerId, request.getMode()));
            }

            VirtualAccountPartnerConfig config = partnerConfigOptional.get();

            log.info("CAPACITY::", config.getCapacity());
            String virtualAccountId = accountPoolService.generateVirtualAccount(config.getCapacity(), Integer.valueOf(config.getPrefix()));
            VirtualAccountCustomer c = customerRepository.getVirtualAccountFromList(virtualAccountId);

            log.info("VirtualAccountCustomer::{}", c);
            boolean isAccountIdExist = false;

            if(c != null){
                isAccountIdExist = true;
                log.info("VirtualAccountCustomer::{}", true);
                VirtualAccountPool accountPool = accountPoolService.getVirtualAccountPool(config.getCapacity(), Integer.valueOf(config.getPrefix()));
                accountPool.setCurrentSequence(accountPool.getCurrentSequence() + 1);
                accountPoolService.updatePool(accountPool);
                //if accountId exist check if it is dynamic and has exceeded its cool down period
                if(c.getMeta().getMode() == VirtualAccountMode.DYNAMIC && LocalDateTime.now().compareTo(c.getExpiryDate()) > 0){
                    isAccountIdExist = false;
                }
            }

            if(isAccountIdExist){
                log.info("Account ID({}) already exist. Please try again", virtualAccountId);
                throw new RestServiceException(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), virtualAccountId);
            }

            log.info("virtualAccountId::{}", virtualAccountId);
            if(request instanceof VirtualAccountRequestDto){
                account = virtualAccountCustomerMapper.dtoToEntity((VirtualAccountRequestDto)request);

                log.info("(VirtualAccountRequestDto)request::[{}]", (VirtualAccountRequestDto)request);
                log.info("VirtualAccount::{}", account);

                //validate partner name and customer name
                validatePartnerAndCustomerName(account.getAccountName(), config.getMeta().getDefaultLookupDisplayName());
            }else {
                account = VirtualAccount.builder().build();
            }
            account.setAccountId(virtualAccountId);
            account.setReferenceId(referenceId);
            account.setPartnerId(request.getPartnerId());
            account.setInvoiceRef(request.getInvoiceRef());
        }
        //validate the account ID in the request
        else{
            //does it exist
            log.info("BLANK ID");
            VirtualAccountCustomer vc = customerRepository.getVirtualAccountFromList(accountId);

            if(vc != null){
                if(vc.getMeta().getMode() == VirtualAccountMode.STATIC){
                    throw new RestServiceException(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), accountId);
                }

                //if accountId exist check if it is dynamic and has exceeded its cool down period
                log.info("Account ID({}) already exist. Please try again compare::{}", accountId, LocalDateTime.now().compareTo(vc.getExpiryDate()));
                if(vc.getMeta().getMode() == VirtualAccountMode.DYNAMIC && LocalDateTime.now().compareTo(vc.getExpiryDate()) < 0){
                    log.info("Account ID({}) already exist. Please try again", accountId);
                    throw new RestServiceException(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), accountId);
                }
            }

            //validate
            boolean isAccountValid = validateVirtualAccountId(accountId, request);
            log.info("isAccountValid::{}", isAccountValid);
            if(!isAccountValid){
                log.info("Account ID({}) failed validation. May not be within allocated range", accountId);
                throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_NUMBER.getCode(), accountId);
            }
            //increment current index
            //accountPoolService.updateCurrentPrefix(config.getCapacity(), Integer.valueOf(config.getPrefix()));
            if(request instanceof VirtualAccountRequestDto){
                account = virtualAccountCustomerMapper.dtoToEntity((VirtualAccountRequestDto)request);
            }else{
                account = VirtualAccount.builder().build();
            }
            account.setAccountId(accountId);
            account.setPartnerId(partnerId);
            account.setReferenceId(referenceId);
            account.setInvoiceRef(request.getInvoiceRef());
        }
        return account;
    }

    @SneakyThrows
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

    @SneakyThrows
    private boolean validateVirtualAccountId(String accountId, BaseVirtualAccountRequestDto request) {
        List<VirtualAccountPartnerConfig> configs = configService.findPartnerConfigByIdentifier(request.getPartnerId());
        if(configs.isEmpty()){
            log.info("Partner config({}) does not exist! Contact admin", request.getPartnerId());
            throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), request.getPartnerId());
        }

        Optional<VirtualAccountPartnerConfig> optionalPartnerConfig = configs.stream().filter(c -> c.getMeta().getMode() == request.getMode()).distinct().findFirst();

        if(!optionalPartnerConfig.isPresent()){
            log.info("Partner config({}) with account mode({}) does not exist! Contact admin", request.getPartnerId(),request.getMode());
            throw new RestServiceException(ErrorCodes.PARTNER_CONFIG_DOES_NOT_EXIST.getCode(), request.getPartnerId());
        }

        VirtualAccountPartnerConfig config = optionalPartnerConfig.get();

        VirtualAccountPool virtualAccountPool = accountPoolService.getVirtualAccountPool(config.getCapacity(), Integer.valueOf(config.getPrefix()));
        String format = "%0"+virtualAccountPool.getTotalUsableDigits()+"d";
        String startRange = virtualAccountPool.getStartPrefix().toString() + String.format(format, 0);
        String endRange = virtualAccountPool.getPrefixEndSeries().toString() + String.format(format, virtualAccountPool.getMaximumRange());

        log.info("STARTRANGE {} ENDRANGE {}", startRange, endRange);

        if(accountId.matches("\\d{10}")) {
            return isWithinSeriesRange(Long.valueOf(accountId), Long.valueOf(startRange), Long.valueOf(endRange));
        }
        return false;

    }

    private VirtualAccountCustomer buildCustomerVirtualAccount(String virtualAccount, String partnerId) {
        VirtualAccountCustomer virtualAccountCustomer = VirtualAccountCustomer.builder()
                .accountId(virtualAccount)
                .accountId(partnerId)
                .build();
        return virtualAccountCustomer;
    }

    private boolean isWithinSeriesRange(Long accountId, Long seriesStart, Long seriesEnd) {
        return accountId.compareTo(seriesStart) >= 0 && accountId.compareTo(seriesEnd) <= 0;
    }

    public List<BaseVirtualAccountResponseDto> createVirtualAccount(List<BaseVirtualAccountRequestDto> requestDtos) {
        log.info("requestDtos::{}", requestDtos.size());
        //BaseVirtualAccountRequestDto[] arrayRequestDtos = requestDtos.toArray(new BaseVirtualAccountRequestDto[requestDtos.size()]);

        //ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        List<BaseVirtualAccountResponseDto> result = forkJoinPool.invoke(new VirtualAccountRecursiveTask(this, virtualAccountCustomerMapper, requestDtos));
        log.info("SIZE::{}",result.size());
        log.info("Parallel Pool Size::{}", ForkJoinPool.commonPool());
        return result;
    }

    public List<VirtualAccount> createVirtualAccount2(List<BaseVirtualAccountRequestDto> requestDtos){
        List<CompletableFuture<VirtualAccount>> virtualAccountFutures = new ArrayList<>();
        for(BaseVirtualAccountRequestDto request: requestDtos) {
            CompletableFuture<VirtualAccount> virtualFuture = CompletableFuture.supplyAsync(() -> createVirtualAccount(request));
            //log.info("virtualFuture::{}", virtualFuture.get());
            virtualAccountFutures.add(virtualFuture);
        }
        CompletableFuture.allOf(virtualAccountFutures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null).join();

        return virtualAccountFutures.stream()
                .filter(virtualAccount -> !virtualAccount.isCompletedExceptionally())
                .map(virtualAccount -> virtualAccount.getNow(VirtualAccount.builder().build()))
                .collect(Collectors.toList());

    }

}

package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.entity.DynamicAccount;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.entity.StaticAccount;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.mapper.AccountCustomerMapper;
import com.keelean.accountmanager.repo.AccountCustomerRepo;
import com.keelean.accountmanager.repo.EntitySessionManager;
import com.keelean.accountmanager.utils.AppUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;


@Slf4j
public abstract class AbstractVirtualAccount implements AccountCreationMode {

    @Autowired
    protected PartnerAccountConfigService configService;
    @Autowired
    protected AccountCustomerRepo customerRepository;
    @Autowired
    private AccountCustomerMapper virtualAccountCustomerMapper;
    @Autowired
    private AccountPoolService accountPoolService;

    @Autowired
    private EntitySessionManager entitySessionManager;

    @Value("${va-partner.dynamic.de-allocation.used-account-cooldown-days.default}")
    private Integer defaultCooldownInDays;

    @SneakyThrows
    @Transactional(value = Transactional.TxType.REQUIRED, dontRollbackOn = Throwable.class)
    public Account createVirtualAccount(BaseAccountRequestDto request) {

        log.info("BaseVirtualAccountRequestDto::[{}]", request);
        String referenceId = request.getReferenceId();
        validateReferenceId(referenceId);

        //Generate new account id if account id is not in the request
        String accountId = request.getAccountId();
        log.info("ACCOUNT ID::{}", accountId);
        Account account = null;
        String partnerId = request.getPartnerId();
        String virtualAccountId;
        PartnerAccountConfig config = configService.getPartnerAccountConfig(request.getAccountType(), partnerId);
        if (StringUtils.isBlank(accountId)) {
            //PartnerAccountConfig config = configService.getPartnerAccountConfig(request.getAccountType(), partnerId);
            if (config.isSharedPool()) {
                //Retrieve shared pool from the db
                AccountPool accountPool = accountPoolService.findPoolByPrefixSeries(Integer.valueOf(config.getPrefix()));
                //Check for daily maximum
                //TODO Check if maximum account for day is reached?
                virtualAccountId = AppUtils.formatEndSequence(accountPool.getCapacity().getReusableDigits(), accountPool.generateSequence());

                //Check if account already exist
                accountPoolService.saveOrUpdatePool(accountPool);
            } else {
                virtualAccountId = AppUtils.formatEndSequence(config.getCapacity().getReusableDigits(), config.generateSequence());
                configService.update(config);
            }

            if (request instanceof AccountRequestDto) {
                account = virtualAccountCustomerMapper.dtoToEntity((AccountRequestDto) request);
                //validate partner name and customer name
                validatePartnerAndCustomerName(account.getAccountName(), config.getMeta().getDefaultLookupDisplayName());
            } else {
                account = newAccount(request.getAccountType());
            }
            buildAccount(request, account, virtualAccountId);

        } else {//Validate the account ID in the request.
            validatePartnerSuppliedAccountVirtualAccount(accountId, config);
            if (request instanceof AccountRequestDto) {
                account = virtualAccountCustomerMapper.dtoToEntity((AccountRequestDto) request);
                validatePartnerAndCustomerName(account.getAccountName(), config.getMeta().getDefaultLookupDisplayName());
            } else {
                account = newAccount(request.getAccountType());
            }
            buildAccount(request, account, accountId);
        }
        return account;
    }

    /*
    private void validateExistingAccountCooldownPeriod(PartnerAccountConfig partnerAccountConfig, String accountId) {
        AccountCustomer accountCustomer = customerRepository.getVirtualAccountFromList(accountId);

        if (Objects.isNull(accountCustomer)) {
            return;
        }

        if (partnerAccountConfig.isSharedPool()) {
            validateCoolDownPeriod(accountCustomer, defaultCooldownInDays);
        } else {
            Integer partnerCoolDownPeriod = partnerAccountConfig.getMeta().getCoolDownPeriod();
            validateCoolDownPeriod(accountCustomer, partnerCoolDownPeriod);
        }
    }*/

    private void validateCoolDownPeriod(AccountCustomer accountCustomer, Integer coolDownPeriod) {
        Duration duration = Duration.between(LocalDateTime.now(), accountCustomer.getLastModifiedDate());
        if (duration.toDays() < coolDownPeriod) {
            throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_STATUS.getCode());
        }
    }

    private void validatePartnerSuppliedAccountVirtualAccount(String accountId, PartnerAccountConfig config) {
        if (accountId.length() != 10) {
            throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_NUMBER.getCode());
        }

        if (config.isSharedPool()) {
            throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
        }

        if (!accountId.startsWith(config.getPrefix())) {
            throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
        }
    }

    private Account newAccount(AccountType accountType) {
        return accountType == AccountType.STATIC ? StaticAccount.builder().build() : DynamicAccount.builder().build();
    }

    private void buildAccount(BaseAccountRequestDto request, Account account, String accountId) {
        account.setAccountId(accountId);
        account.setPartnerId(request.getPartnerId());
        account.setReferenceId(request.getReferenceId());
        account.setInvoiceRef(request.getInvoiceRef());
    }

    private void validateReferenceId(String referenceId) {
        if (Objects.isNull(referenceId) || Strings.isBlank(referenceId)) {
            throw new RestServiceException(ErrorCodes.INVALID_OR_EMPTY_ACCOUNT_ID_OR_REFERENCE.getCode(), referenceId);
        }

        AccountCustomer customer = customerRepository.getVirtualAccountFromList(referenceId);

        if (customer != null) {
            throw new RestServiceException(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), referenceId);
        }
    }


    @SneakyThrows
    private void validatePartnerAndCustomerName(String accountName, String partnerName) {

        if (Objects.isNull(accountName)) {
            throw new RestServiceException(ErrorCodes.VALIDATION_FAILED.getCode(), partnerName);
        }

        if (!accountName.startsWith(partnerName)) {
            log.info("{} must contain {} as a prefix or suffix", accountName, partnerName);
            throw new RestServiceException(ErrorCodes.ACCOUNT_NAME_VALIDATION_FAILED.getCode(), accountName, partnerName);
        }

        if (accountName.length() > 30) {
            log.info("{} must contain is more than 30 characters long", accountName);
            throw new RestServiceException(ErrorCodes.ACCOUNT_LENGTH_VALIDATION.getCode(), accountName);
        }
    }


}

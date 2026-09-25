package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.AccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.entity.DynamicAccount;
import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.entity.StaticAccount;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.mapper.AccountCustomerMapper;
import com.keelean.accountmanager.repo.AccountCustomerRepo;
import com.keelean.accountmanager.repo.EntitySessionManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
public abstract class AbstractVirtualAccount implements AccountCreationMode {

    @Autowired
    protected PartnerAccountConfigService configService;
    @Autowired
    protected AccountCustomerRepo customerRepository;
    @Autowired
    private AccountCustomerMapper virtualAccountCustomerMapper;
    @Autowired
    private SequenceAllocator sequenceAllocator;

    @Autowired
    private EntitySessionManager entitySessionManager;

    @Value("${va-partner.dynamic.de-allocation.used-account-cooldown-days.default}")
    private Integer defaultCooldownInDays;

    public Account createVirtualAccount(BaseAccountRequestDto request) {
        return createVirtualAccounts(List.of(request)).get(0);
    }

    /**
     * Builds unsaved accounts for the requests, in request order. Every request is validated before any
     * sequence is reserved, and each partner config's IDs are reserved in one locked update.
     * Deliberately not @Transactional: an open transaction here would hold a pooled connection while
     * the allocator takes a second one, which can exhaust the pool under concurrent requests.
     */
    public List<Account> createVirtualAccounts(List<? extends BaseAccountRequestDto> requests) {
        log.info("Creating {} accounts", requests.size());
        validateReferenceIds(requests);

        Map<String, PartnerAccountConfig> configsByPartnerAndType = new HashMap<>();
        Map<Long, List<PendingAccount>> awaitingIdsByConfigId = new LinkedHashMap<>();
        List<Account> accounts = new ArrayList<>(requests.size());

        for (BaseAccountRequestDto request : requests) {
            PartnerAccountConfig config = configsByPartnerAndType.computeIfAbsent(
                    request.getPartnerId() + "|" + request.getAccountType(),
                    key -> configService.getPartnerAccountConfig(request.getAccountType(), request.getPartnerId()));

            String accountId = request.getAccountId();
            if (StringUtils.isNotBlank(accountId)) {
                validatePartnerSuppliedAccountVirtualAccount(accountId, config);
            }
            Account account = toAccount(request, config);
            if (StringUtils.isBlank(accountId)) {
                awaitingIdsByConfigId.computeIfAbsent(config.getId(), id -> new ArrayList<>())
                        .add(new PendingAccount(request, account, config));
            } else {
                buildAccount(request, account, accountId);
            }
            accounts.add(account);
        }

        // Generate account IDs only once every request is valid
        for (List<PendingAccount> pending : awaitingIdsByConfigId.values()) {
            List<String> accountIds = sequenceAllocator.reserveAccountIds(pending.get(0).config(), pending.size());
            for (int i = 0; i < pending.size(); i++) {
                buildAccount(pending.get(i).request(), pending.get(i).account(), accountIds.get(i));
            }
        }
        return accounts;
    }

    private record PendingAccount(BaseAccountRequestDto request, Account account, PartnerAccountConfig config) {
    }

    private Account toAccount(BaseAccountRequestDto request, PartnerAccountConfig config) {
        if (request instanceof AccountRequestDto) {
            Account account = virtualAccountCustomerMapper.dtoToEntity((AccountRequestDto) request);
            //validate partner name and customer name
            validatePartnerAndCustomerName(account.getAccountName(), config.getMeta().getDefaultLookupDisplayName());
            return account;
        }
        return newAccount(request.getAccountType());
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

    private void validateReferenceIds(List<? extends BaseAccountRequestDto> requests) {
        for (BaseAccountRequestDto request : requests) {
            String referenceId = request.getReferenceId();
            if (Objects.isNull(referenceId) || Strings.isBlank(referenceId)) {
                throw new RestServiceException(ErrorCodes.INVALID_OR_EMPTY_ACCOUNT_ID_OR_REFERENCE.getCode(), referenceId);
            }
        }

        // A reference is taken if it matches an existing reference or account ID
        Set<String> referenceIds = requests.stream().map(BaseAccountRequestDto::getReferenceId).collect(Collectors.toSet());
        Set<String> taken = new HashSet<>();
        for (AccountCustomer customer : customerRepository.findByReferenceIdInOrAccountIdIn(referenceIds, referenceIds)) {
            taken.add(customer.getReferenceId());
            taken.add(customer.getAccountId());
        }
        for (BaseAccountRequestDto request : requests) {
            if (taken.contains(request.getReferenceId())) {
                throw new RestServiceException(ErrorCodes.ACCOUNT_ALREADY_EXISTS.getCode(), request.getReferenceId());
            }
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

package com.keelean.accountmanager.service.impl;

import com.keelean.accountmanager.dto.FullAccountResponseDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.entity.AccountMeta;
import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.AccountStatus;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.exception.RestServiceException;
import com.keelean.accountmanager.repo.EntitySessionManager;
import com.keelean.accountmanager.service.AbstractVirtualAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component("static")
@Slf4j
public class StaticVirtualAccount extends AbstractVirtualAccount {

    @Autowired
    private EntitySessionManager entitySessionManager;

    @Override
    public FullAccountResponseDto singleFullCreation(Account virtualAccount) {
        AccountCustomer virtualAccountCustomer = toFullCustomer(virtualAccount);
        entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("Created static account {} for reference {}", virtualAccount.getAccountId(), virtualAccount.getReferenceId());
        return FullAccountResponseDto.builder()
                .accountId(virtualAccount.getAccountId())
                .expiryDate(virtualAccountCustomer.getExpiryDate())
                .referenceId(virtualAccount.getReferenceId())
                .build();
    }

    @Override
    public AccountCustomer toFullCustomer(Account virtualAccount) {
        AccountMode mode = AccountMode.STATIC_NORMAL;
        // Invoice accounts are always created as closed-on-payment
        if (Objects.nonNull(virtualAccount.getInvoiceRef())) {
            mode = AccountMode.STATIC_INVOICE_CLOSED;
        }

        Integer minDeposit = null;
        Integer maxDeposit = null;

        // Deposits default to 0, meaning the partner's exact-amount rule applies
        if (isSet(virtualAccount.getMinDeposit()) && isSet(virtualAccount.getMaxDeposit())) {
            minDeposit = Math.toIntExact(virtualAccount.getMinDeposit());
            maxDeposit = Math.toIntExact(virtualAccount.getMaxDeposit());

            boolean isMinDepositValid = minDeposit >= 1 && minDeposit <= 10;
            boolean isMaxDepositValid = maxDeposit >= 10 && maxDeposit <= 100;

            if (!isMinDepositValid || !isMaxDepositValid) {
                throw new RestServiceException(ErrorCodes.INCOMPLETE_OR_WRONG_CONFIGURATION.getCode());
            }
        }

        return AccountCustomer.builder()
                .meta(AccountMeta.builder()
                        .amount(virtualAccount.getAmount())
                        .waitStartTime(DynamicVirtualAccount.toWaitStartTime(virtualAccount.getWaitStartTime()))
                        .accountName(virtualAccount.getAccountName())
                        .accountType(AccountType.STATIC)
                        .minMultiplier(minDeposit)
                        .maxMultiplier(maxDeposit)
                        .invoicePaymentRef(virtualAccount.getInvoiceRef())
                        .build())
                .expiryDate(LocalDateTime.now().plus(1, ChronoUnit.YEARS))
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .status(AccountStatus.CREATED)
                .mode(mode)
                .partnerId(virtualAccount.getPartnerId())
                .build();
    }

    private static boolean isSet(Long deposit) {
        return deposit != null && deposit > 0;
    }
}

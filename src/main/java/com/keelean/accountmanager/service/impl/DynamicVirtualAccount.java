package com.keelean.accountmanager.service.impl;

import com.keelean.accountmanager.constants.AppConstants;
import com.keelean.accountmanager.dto.BaseAccountRequestDto;
import com.keelean.accountmanager.dto.BaseAccountResponseDto;
import com.keelean.accountmanager.dto.FullAccountResponseDto;
import com.keelean.accountmanager.entity.Account;
import com.keelean.accountmanager.entity.AccountCustomer;
import com.keelean.accountmanager.entity.AccountMeta;
import com.keelean.accountmanager.enums.AccountMode;
import com.keelean.accountmanager.enums.AccountStatus;
import com.keelean.accountmanager.enums.AccountType;
import com.keelean.accountmanager.repo.EntitySessionManager;
import com.keelean.accountmanager.service.AbstractVirtualAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component("dynamic")
public class DynamicVirtualAccount extends AbstractVirtualAccount {

    @Autowired
    private EntitySessionManager entitySessionManager;

    @Override
    public FullAccountResponseDto singleFullCreation(Account virtualAccount) {
        long timeout = virtualAccount.getTimeoutInMins() == null
                ? AppConstants.DYNAMIC_PAYMENT_WINDOW_MINS
                : Math.max(virtualAccount.getTimeoutInMins(), AppConstants.DYNAMIC_PAYMENT_WINDOW_MINS);

        AccountCustomer virtualAccountCustomer = AccountCustomer.builder()
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .expiryDate(LocalDateTime.now().plus(AppConstants.DYNAMIC_ACCOUNT_EXPIRY_DAYS, ChronoUnit.DAYS))
                .status(AccountStatus.CREATED)
                .meta(AccountMeta.builder()
                        .accountName(virtualAccount.getAccountName())
                        .waitStartTime(LocalDateTime.now().plus(timeout, ChronoUnit.MINUTES))
                        .accountType(AccountType.DYNAMIC)
                        .amount(virtualAccount.getAmount())
                        .build())
                .mode(AccountMode.DYNAMIC)
                .partnerId(virtualAccount.getPartnerId())
                .build();
        entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("Created dynamic account {} for reference {}", virtualAccount.getAccountId(), virtualAccount.getReferenceId());
        return FullAccountResponseDto.builder()
                .expiryDate(virtualAccountCustomer.getExpiryDate())
                .referenceId(virtualAccount.getReferenceId())
                .accountId(virtualAccount.getAccountId())
                .build();
    }

    @Override
    public List<BaseAccountResponseDto> preCreationBulkMode(List<BaseAccountRequestDto> requestDtos) {
        return super.preCreationBulkMode(requestDtos);
    }

    // Reserves the account number; the account is completed later through the update endpoint
    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = Throwable.class)
    @Override
    public BaseAccountResponseDto preCreation(Account virtualAccount) {
        AccountCustomer virtualAccountCustomer = AccountCustomer.builder()
                .meta(AccountMeta.builder()
                        .amount(virtualAccount.getAmount())
                        .accountName(virtualAccount.getAccountName())
                        .waitStartTime(toWaitStartTime(virtualAccount.getWaitStartTime()))
                        .accountType(AccountType.DYNAMIC)
                        .build())
                .partnerId(virtualAccount.getPartnerId())
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .status(AccountStatus.WAITING)
                .mode(AccountMode.DYNAMIC)
                .build();
        virtualAccountCustomer = entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("Pre-created dynamic account {} for reference {}", virtualAccountCustomer.getAccountId(), virtualAccountCustomer.getReferenceId());
        return BaseAccountResponseDto.builder()
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .build();
    }

    @Override
    public List<BaseAccountResponseDto> preCreation(List<Account> virtualAccounts) {
        List<CompletableFuture<BaseAccountResponseDto>> virtualAccountFutures = new ArrayList<>();
        for (Account request : virtualAccounts) {
            CompletableFuture<BaseAccountResponseDto> virtualAccountFuture = CompletableFuture.supplyAsync(
                    () -> preCreation(request));
            virtualAccountFutures.add(virtualAccountFuture);
        }
        CompletableFuture.allOf(virtualAccountFutures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null).join();

        return virtualAccountFutures.stream()
                .filter(virtualAccountFuture -> !virtualAccountFuture.isCompletedExceptionally())
                .map(campaignFuture -> campaignFuture.getNow(BaseAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
    }

    // Account keeps the wait start as an offset in hours; the stored meta keeps the resulting time
    static LocalDateTime toWaitStartTime(Integer waitStartTimeInHours) {
        return waitStartTimeInHours == null ? null : LocalDateTime.now().plusHours(waitStartTimeInHours);
    }
}

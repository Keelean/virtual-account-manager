package com.keelean.legacy.customeraccounts.service.impl;

import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountRequestDto;
import com.keelean.legacy.customeraccounts.dto.FullVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.dto.BaseVirtualAccountResponseDto;
import com.keelean.legacy.customeraccounts.entity.VirtualAccount;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountMeta;
import com.keelean.legacy.customeraccounts.entity.VirtualAccountCustomer;
import com.keelean.legacy.customeraccounts.enums.AccountMode;
import com.keelean.legacy.customeraccounts.enums.AccountStatus;
import com.keelean.legacy.customeraccounts.enums.VirtualAccountMode;
import com.keelean.legacy.customeraccounts.service.AbstractVirtualAccount;
import com.example.platform.dao.EntitySessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component("dynamic")
public class DynamicVirtualAccount extends AbstractVirtualAccount {

    private static long TIMEOUT = 1440;

    @Autowired
    private EntitySessionManager entitySessionManager;

    //@Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = Throwable.class)
    @Override
    public FullVirtualAccountResponseDto singleFullCreation(VirtualAccount virtualAccount) {
        long timeout = 0;
        if(virtualAccount.getTimeoutInMins() < TIMEOUT){
            timeout = TIMEOUT;
        }else {
            timeout = virtualAccount.getTimeoutInMins();
        }
        VirtualAccountCustomer virtualAccountCustomer = VirtualAccountCustomer.builder()
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .expiryDate(LocalDateTime.now().plus(90, ChronoUnit.DAYS))
                .status(AccountStatus.CREATED)
                .meta(VirtualAccountMeta.builder()
                        .accountName(virtualAccount.getAccountName())
                        //.timeoutInMins(virtualAccount.getTimeoutInMins())
                        .waitStartTime(LocalDateTime.now().plus(timeout, ChronoUnit.HOURS))
                        .mode(virtualAccount.getMode())
                        .amount(virtualAccount.getAmount())
                        .build())
                .mode(AccountMode.NORMAL)
                .partnerId(virtualAccount.getPartnerId())
                .build();
        entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("customize(VirtualAccount virtualAccount)");
        return FullVirtualAccountResponseDto.builder()
                .expiryDate(virtualAccountCustomer.getExpiryDate())
                .referenceId(virtualAccount.getReferenceId())
                .accountId(virtualAccount.getAccountId())
                .build();
    }


    @Override
    public List<BaseVirtualAccountResponseDto> preCreationBulkMode(List<BaseVirtualAccountRequestDto> requestDtos) {

        return super.preCreationBulkMode(requestDtos);
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = Throwable.class)
    @Override
    public BaseVirtualAccountResponseDto preCreation(VirtualAccount virtualAccount) {
        VirtualAccountCustomer virtualAccountCustomer = VirtualAccountCustomer.builder()
                .meta(VirtualAccountMeta.builder()
                        .amount(virtualAccount.getAmount())
                        .accountName(virtualAccount.getAccountName())
                        .mode(virtualAccount.getMode())
                        .waitStartTime(virtualAccount.getWaitStartTime())
                        //.timeoutInMins(virtualAccount.getTimeoutInMins())
                        .mode(VirtualAccountMode.DYNAMIC)
                        .build())
                .partnerId(virtualAccount.getPartnerId())
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .status(AccountStatus.WAITING)
                .mode(AccountMode.NORMAL)
                .build();
        log.info("VirtualAccount0::{}", virtualAccountCustomer.getReferenceId());
        log.info("VirtualAccount1::{}", virtualAccountCustomer.getAccountId());
        log.info("VirtualAccount2::{}", virtualAccount.getAccountId());
        virtualAccountCustomer = entitySessionManager.saveOrUpdateCommit(virtualAccountCustomer);
        log.info("VirtualAccount4::{}", virtualAccountCustomer);
        BaseVirtualAccountResponseDto baseVirtualAccountResponseDto = BaseVirtualAccountResponseDto.builder()
                .accountId(virtualAccount.getAccountId())
                .referenceId(virtualAccount.getReferenceId())
                .build();

        return baseVirtualAccountResponseDto;
    }

    public List<BaseVirtualAccountResponseDto> preCreation(List<VirtualAccount> virtualAccounts) {
        List<CompletableFuture<BaseVirtualAccountResponseDto>> virtualAccountFutures = new ArrayList<>();
        for(VirtualAccount request: virtualAccounts){
            CompletableFuture<BaseVirtualAccountResponseDto> virtualAccountFuture = CompletableFuture.supplyAsync(
                    () -> preCreation(request));
            virtualAccountFutures.add(virtualAccountFuture);
        }
        CompletableFuture.allOf(virtualAccountFutures.toArray(new CompletableFuture[4]))
                .exceptionally(ex -> null).join();

        return virtualAccountFutures.stream()
                .filter(virtualAccountFuture -> !virtualAccountFuture.isCompletedExceptionally())
                .map(campaignFuture -> campaignFuture.getNow(BaseVirtualAccountResponseDto.builder().build()))
                .collect(Collectors.toList());
    }
}

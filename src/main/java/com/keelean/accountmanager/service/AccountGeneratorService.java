package com.keelean.accountmanager.service;


import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import com.keelean.accountmanager.exception.ErrorCodes;
import com.keelean.accountmanager.repo.AccountPoolRepo;
import com.keelean.accountmanager.repo.EntitySessionManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AccountGeneratorService {

    @Autowired
    AccountPoolRepo repository;

    /*
    @PersistenceContext
    private EntityManager entityManager;*/

    @Autowired
    EntitySessionManager entitySessionManager;

    @SneakyThrows
    //@Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = Throwable.class)
    public String generateAccount(AccountCapacity accountCapacity, int currentPrefix) {
        AccountPool virtualAccountPool = getVirtualAccountPool(accountCapacity, currentPrefix);

        log.info("accountCapacity::{}",accountCapacity);
        log.info("currentPrefix::{}",currentPrefix);

        if(virtualAccountPool == null){
            log.info("Account range does not exist! Contact admin.");
            //throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
            throw new RuntimeException();
        }

        Integer newSequence = virtualAccountPool.getCurrentSequence() + 1;

        log.info("newSequence::{}", newSequence);

        String newVirtualAccount = null;

        String format = "%d%0"+virtualAccountPool.getTotalUsableDigits()+"d";

        if(!isNewSequenceExceedMaxRange(newSequence, virtualAccountPool)){
            //Pad with zeros
            newVirtualAccount = String.format(format, virtualAccountPool.getCurrentPrefix(), newSequence);
            virtualAccountPool.setCurrentSequence(newSequence);
            return newVirtualAccount;
        }else{
            Integer newCurrentPrefix = virtualAccountPool.getCurrentPrefix() + 1;
            if(isCurrentPrefixExhausted(newCurrentPrefix, virtualAccountPool)){
                newSequence = 1;
                newVirtualAccount = String.format(format, newCurrentPrefix, newSequence);
                virtualAccountPool.setCurrentSequence(newSequence);
                virtualAccountPool.setCurrentPrefix(newCurrentPrefix);
                entitySessionManager.saveOrUpdateCommit(virtualAccountPool);
            }else{
                virtualAccountPool.setState(State.CLOSED);
                entitySessionManager.saveOrUpdateCommit(virtualAccountPool);
            }
        }

        if(newVirtualAccount == null){
            log.info("Account Range exceeded. Contact admin");
            //throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
            throw new RuntimeException();
        }
        return newVirtualAccount;
    }

    private boolean isNewSequenceExceedMaxRange(Integer newSequence, AccountPool accountPool){
        if(newSequence > accountPool.getMaximumRange()){
            return true;
        }
        return false;
    }

    private boolean isCurrentPrefixExhausted(Integer newCurrentPrefix, AccountPool accountPool){
        if(newCurrentPrefix < accountPool.getPrefixEndSeries()){
            return true;
        }
        return false;
    }

    public AccountPool getVirtualAccountPool(AccountCapacity accountCapacity, Integer currentPrefix){
        List<AccountPool> virtualAccountPools = repository.findByCapacityAndStartPrefixAndState(accountCapacity, currentPrefix, State.OPEN);
        if(virtualAccountPools.isEmpty()){
            return null;
        }
        return virtualAccountPools.get(0);
    }

    @SneakyThrows
    public void updateCurrentPrefix(AccountCapacity accountCapacity, int currentPrefix){
        AccountPool virtualAccountPool = getVirtualAccountPool(accountCapacity, currentPrefix);

        if(virtualAccountPool == null){
            log.info("Account range does not exist! Contact admin.");
            //throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
            throw new RuntimeException();
        }

        virtualAccountPool.setCurrentPrefix(virtualAccountPool.getCurrentPrefix() + 1);
        entitySessionManager.saveOrUpdateCommit(virtualAccountPool);
    }

    public void updatePool(AccountPool accountPool){
        entitySessionManager.saveOrUpdateCommit(accountPool);
    }
}

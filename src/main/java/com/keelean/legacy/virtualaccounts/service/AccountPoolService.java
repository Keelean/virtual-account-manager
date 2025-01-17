package com.keelean.legacy.customeraccounts.service;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountPool;
import com.keelean.legacy.customeraccounts.enums.AccountCapacity;
import com.keelean.legacy.customeraccounts.enums.State;
import com.keelean.legacy.customeraccounts.exception.ErrorCodes;
import com.keelean.legacy.customeraccounts.repo.VirtualAccountPoolRepo;
import com.example.platform.dao.EntitySessionManager;
import com.example.platform.exception.RestServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Component
public class VirtualAccountPoolService {

    @Autowired
    VirtualAccountPoolRepo repository;

    /*
    @PersistenceContext
    private EntityManager entityManager;*/

    @Autowired
    EntitySessionManager entitySessionManager;

    @SneakyThrows
    //@Transactional(value = Transactional.TxType.REQUIRES_NEW, dontRollbackOn = Throwable.class)
    public String generateVirtualAccount(AccountCapacity accountCapacity, int currentPrefix) {
        VirtualAccountPool virtualAccountPool = getVirtualAccountPool(accountCapacity, currentPrefix);

        log.info("accountCapacity::{}",accountCapacity);
        log.info("currentPrefix::{}",currentPrefix);

        if(virtualAccountPool == null){
            log.info("Account range does not exist! Contact admin.");
            throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
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
            throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
        }
        return newVirtualAccount;
    }

    private boolean isNewSequenceExceedMaxRange(Integer newSequence, VirtualAccountPool accountPool){
        if(newSequence > accountPool.getMaximumRange()){
            return true;
        }
        return false;
    }

    private boolean isCurrentPrefixExhausted(Integer newCurrentPrefix, VirtualAccountPool accountPool){
        if(newCurrentPrefix < accountPool.getPrefixEndSeries()){
            return true;
        }
        return false;
    }

    public VirtualAccountPool getVirtualAccountPool(AccountCapacity accountCapacity, Integer currentPrefix){
        List<VirtualAccountPool> virtualAccountPools = repository.findByCapacityAndStartPrefixAndState(accountCapacity, currentPrefix, State.OPEN);
        if(virtualAccountPools.isEmpty()){
            return null;
        }
        return virtualAccountPools.get(0);
    }

    @SneakyThrows
    public void updateCurrentPrefix(AccountCapacity accountCapacity, int currentPrefix){
        VirtualAccountPool virtualAccountPool = getVirtualAccountPool(accountCapacity, currentPrefix);

        if(virtualAccountPool == null){
            log.info("Account range does not exist! Contact admin.");
            throw new RestServiceException(ErrorCodes.INVALID_ACCOUNT_RANGE.getCode());
        }

        virtualAccountPool.setCurrentPrefix(virtualAccountPool.getCurrentPrefix() + 1);
        entitySessionManager.saveOrUpdateCommit(virtualAccountPool);
    }

    public void updatePool(VirtualAccountPool accountPool){
        entitySessionManager.saveOrUpdateCommit(accountPool);
    }
}

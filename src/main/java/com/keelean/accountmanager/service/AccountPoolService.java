package com.keelean.accountmanager.service;


import com.keelean.accountmanager.dto.AccountPoolDto;
import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.repo.AccountPoolRepo;
import com.keelean.accountmanager.repo.EntitySessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AccountPoolService {

    @Autowired
    protected AccountPoolRepo accountPoolRepo;

    @Value("${va-pool.prefix-series.range}")
    protected List<String> prefixSeriesRange;

    @Value("${va-pool.prefix-series.allow-creation-unutilized}")
    protected boolean allowCreationUnutilized;

    @Autowired
    EntitySessionManager entitySessionManager;


    public AccountPool createAccountPool(AccountPoolDto accountPoolDto) {
        validatePrefixSeries(accountPoolDto);
        return createPool(accountPoolDto);
    }

    public AccountPool saveOrUpdatePool(AccountPool accountPool) {
        return entitySessionManager.saveOrUpdateCommit(accountPool);
    }

    public List<AccountPool> findPoolByCapacity(AccountCapacity accountCapacity) {
        List<AccountPool> optionalAccountPool = accountPoolRepo.findByCapacity(accountCapacity);
        if (!optionalAccountPool.isEmpty()) {
            return optionalAccountPool;
        }
        throw new IllegalArgumentException("Account capacity does not exist!");
    }

    public AccountPool findPoolByPrefixSeries(Integer prefixSeries) {
        Optional<AccountPool> optionalAccountPool = accountPoolRepo.findByPrefixSeries(prefixSeries);
        if (optionalAccountPool.isPresent()) {
            return optionalAccountPool.get();
        }
        throw new IllegalArgumentException("Account prefix series does not exist!");
    }

    public AccountPool findPoolByPrefixSeriesAndCapacity(Integer prefixSeries, AccountCapacity capacity) {
        List<AccountPool> accountPoolList = accountPoolRepo.findByPrefixSeriesAndCapacityOrderByCreatedDateDesc(prefixSeries, capacity);
        if (!accountPoolList.isEmpty()) {
            return accountPoolList.get(0);
        }
        throw new IllegalArgumentException("Account prefix series and capacity does not exist!");
    }

    private void validatePrefixSeries(AccountPoolDto accountPoolDto) {
        if (!prefixSeriesRange.contains(String.valueOf(accountPoolDto.getPrefixSeries()))) {
            throw new IllegalArgumentException("Prefix series is invalid!");
        }
    }

    abstract AccountPool createPool(AccountPoolDto accountPoolDto);


}

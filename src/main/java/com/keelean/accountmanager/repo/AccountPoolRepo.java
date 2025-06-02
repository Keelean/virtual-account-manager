package com.keelean.accountmanager.repo;

import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountPoolRepo extends JpaRepository<AccountPool, Long> {

    List<AccountPool> findByCapacityAndStartPrefixAndState(AccountCapacity accountCapacity, Integer currentPrefix, State state);
    Optional<AccountPool> findByPrefixSeries(Integer currentPrefix);
    List<AccountPool> findByCapacity(AccountCapacity accountCapacity);
    List<AccountPool> findByPrefixSeriesAndCapacityOrderByCreatedDateDesc(Integer currentPrefix, AccountCapacity capacity);
    Optional<AccountPool> findByCapacityAndStartPrefix(AccountCapacity accountCapacity, Integer currentPrefix);
}

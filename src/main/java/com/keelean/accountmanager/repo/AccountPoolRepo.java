package com.keelean.accountmanager.repo;

import com.keelean.accountmanager.entity.AccountPool;
import com.keelean.accountmanager.enums.AccountCapacity;
import com.keelean.accountmanager.enums.State;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountPoolRepo extends JpaRepository<AccountPool, Long> {

    List<AccountPool> findByCapacityAndStartPrefixAndState(AccountCapacity accountCapacity, Integer currentPrefix, State state);
    Optional<AccountPool> findByPrefixSeries(Integer currentPrefix);

    // SELECT ... FOR UPDATE: serializes sequence reservations for a shared pool
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from AccountPool p where p.prefixSeries = :prefixSeries")
    Optional<AccountPool> findLockedByPrefixSeries(@Param("prefixSeries") Integer prefixSeries);
    List<AccountPool> findByCapacity(AccountCapacity accountCapacity);
    List<AccountPool> findByPrefixSeriesAndCapacityOrderByCreatedDateDesc(Integer currentPrefix, AccountCapacity capacity);
    Optional<AccountPool> findByCapacityAndStartPrefix(AccountCapacity accountCapacity, Integer currentPrefix);
}

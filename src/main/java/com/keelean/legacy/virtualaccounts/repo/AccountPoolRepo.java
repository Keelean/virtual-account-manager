package com.keelean.legacy.customeraccounts.repo;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountPool;
import com.keelean.legacy.customeraccounts.enums.AccountCapacity;
import com.keelean.legacy.customeraccounts.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualAccountPoolRepo extends JpaRepository<VirtualAccountPool, Long> {

    List<VirtualAccountPool> findByCapacityAndStartPrefixAndState(AccountCapacity accountCapacity, Integer currentPrefix, State state);
}

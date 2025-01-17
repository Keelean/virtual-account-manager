package com.keelean.legacy.customeraccounts.repo;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualAccountTransactionRepo extends JpaRepository<VirtualAccountTransaction, Long> {

    VirtualAccountTransaction findByTransactionId(String txnId);
}

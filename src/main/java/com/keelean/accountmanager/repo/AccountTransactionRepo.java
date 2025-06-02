package com.keelean.accountmanager.repo;

import com.keelean.accountmanager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountTransactionRepo extends JpaRepository<Transaction, Long> {

    Transaction findByTransactionId(String txnId);
}

package com.keelean.legacy.customeraccounts.repo;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountActivityRepo extends JpaRepository<VirtualAccountActivity, Long> {

    Optional<VirtualAccountActivity> findByAccountId(String accountId);
}

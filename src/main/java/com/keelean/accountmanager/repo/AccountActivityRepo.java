package com.keelean.accountmanager.repo;

import com.keelean.accountmanager.entity.AccountActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountActivityRepo extends JpaRepository<AccountActivity, Long> {

    Optional<AccountActivity> findByAccountId(String accountId);
}

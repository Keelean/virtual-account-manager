package com.keelean.legacy.customeraccounts.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetryConfigRepo extends JpaRepository<RetryConfig, String> {

    Page<RetryConfig> findByPartnerCodeContains(String partnerCode, Pageable pageable);

}

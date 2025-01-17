package com.keelean.legacy.customeraccounts.repo;

import com.keelean.legacy.customeraccounts.entity.VirtualAccountPartnerConfig;
import com.keelean.legacy.customeraccounts.enums.ConfigStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualAccountPartnerConfigRepo extends JpaRepository<VirtualAccountPartnerConfig, Long> {

    List<VirtualAccountPartnerConfig> findByPartnerIdAndStatus(String partnerId, ConfigStatus status);

    List<VirtualAccountPartnerConfig> findByPartnerId(String partnerId);
}

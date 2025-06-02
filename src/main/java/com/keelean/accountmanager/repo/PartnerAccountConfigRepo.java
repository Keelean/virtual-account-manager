package com.keelean.accountmanager.repo;


import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.enums.ConfigStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerAccountConfigRepo extends JpaRepository<PartnerAccountConfig, Long> {

    List<PartnerAccountConfig> findByPartnerIdAndStatus(String partnerId, ConfigStatus status);

    List<PartnerAccountConfig> findByPartnerId(String partnerId);

    List<PartnerAccountConfig> findByPrefixLike(String prefix);
    List<PartnerAccountConfig> findByPrefixStartsWith(String prefix);
}

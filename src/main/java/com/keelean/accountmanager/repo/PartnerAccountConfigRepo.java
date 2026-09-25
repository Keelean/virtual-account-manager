package com.keelean.accountmanager.repo;


import com.keelean.accountmanager.entity.PartnerAccountConfig;
import com.keelean.accountmanager.enums.ConfigStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerAccountConfigRepo extends JpaRepository<PartnerAccountConfig, Long> {

    List<PartnerAccountConfig> findByPartnerIdAndStatus(String partnerId, ConfigStatus status);

    List<PartnerAccountConfig> findByPartnerId(String partnerId);

    // SELECT ... FOR UPDATE: serializes sequence reservations for a dedicated pool
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PartnerAccountConfig c where c.id = :id")
    Optional<PartnerAccountConfig> findLockedById(@Param("id") Long id);

    List<PartnerAccountConfig> findByPrefixLike(String prefix);
    List<PartnerAccountConfig> findByPrefixStartsWith(String prefix);
}

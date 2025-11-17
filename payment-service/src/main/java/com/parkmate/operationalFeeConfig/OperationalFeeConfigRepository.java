package com.parkmate.operationalFeeConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OperationalFeeConfigRepository extends JpaRepository<OperationalFeeConfigEntity, Long>, JpaSpecificationExecutor<OperationalFeeConfigEntity> {

    @Query("SELECT c FROM OperationalFeeConfigEntity c " +
           "WHERE c.isActive = true " +
           "AND c.validFrom <= :now " +
           "AND (c.validUntil IS NULL OR c.validUntil > :now) " +
           "ORDER BY c.validFrom DESC LIMIT 1")
    Optional<OperationalFeeConfigEntity> findActiveConfig(LocalDateTime now);
}

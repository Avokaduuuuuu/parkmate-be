package com.parkmate.pricing_rule;

import com.parkmate.session.enums.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, Long> {
    List<PricingRuleEntity> findAllByParkingLotIdAndSyncStatus(Long lotId, SyncStatus syncStatus);
}

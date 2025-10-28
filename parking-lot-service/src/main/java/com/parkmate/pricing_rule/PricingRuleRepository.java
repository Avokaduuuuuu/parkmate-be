package com.parkmate.pricing_rule;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, Long> {
    List<PricingRuleEntity> findAllByParkingLotIdAndSyncStatus(Long lotId, SyncStatus syncStatus);
    Optional<PricingRuleEntity> findByIsActiveAndVehicleType(Boolean isActive, VehicleType vehicleType);


    @Modifying
    @Query(
            "UPDATE PricingRuleEntity p " +
                    "SET p.syncStatus = :status " +
                    "WHERE p.id IN :ids"
    )
    Integer updateSyncedPricingRules(@Param("ids") List<Long> ruleIds,@Param("status") SyncStatus syncStatus);
}

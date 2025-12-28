package com.parkmate.pricing_rule;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, Long> {
    List<PricingRuleEntity> findAllByParkingLotIdAndSyncStatus(Long lotId, SyncStatus syncStatus);

    Optional<PricingRuleEntity> findByIsActiveAndVehicleType(Boolean isActive, VehicleType vehicleType);


    @Modifying
    @Query(
            "UPDATE PricingRuleEntity p " +
                    "SET p.syncStatus = :status " +
                    "WHERE p.id IN :ids"
    )
    Integer updateSyncedPricingRules(@Param("ids") List<Long> ruleIds, @Param("status") SyncStatus syncStatus);

    List<PricingRuleEntity> findAllByVehicleTypeAndIsActive(VehicleType vehicleType, Boolean isActive);

    /**
     * Find all pricing rules by parking lot ID, vehicle type, and active status.
     * Used to deactivate other rules of the same type within the SAME parking lot only.
     */
    List<PricingRuleEntity> findAllByParkingLot_IdAndVehicleTypeAndIsActive(Long parkingLotId, VehicleType vehicleType, Boolean isActive);

    Optional<PricingRuleEntity> findByParkingLot_IdAndIsActiveAndVehicleType(Long parkingLotId, Boolean isActive, VehicleType vehicleType);
}

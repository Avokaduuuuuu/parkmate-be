package com.parkmate.default_pricing_rule;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.default_pricing_rule.id.DefaultPricingRuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefaultPricingRuleRepository extends JpaRepository<DefaultPricingRuleEntity, DefaultPricingRuleId> {
    Optional<DefaultPricingRuleEntity> findByIdParkingLotIdAndIdVehicleType(Long lotId, VehicleType vehicleType);

    boolean existsByIdParkingLotIdAndIdVehicleType(Long lotId, VehicleType vehicleType);
}

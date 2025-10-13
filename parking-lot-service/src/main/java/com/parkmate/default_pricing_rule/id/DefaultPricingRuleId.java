package com.parkmate.default_pricing_rule.id;

import com.parkmate.common.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultPricingRuleId implements Serializable {
    @Column(name = "lot_id")
    Long parkingLotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    VehicleType vehicleType;
}

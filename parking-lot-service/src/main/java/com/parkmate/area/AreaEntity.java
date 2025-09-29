package com.parkmate.area;

import com.parkmate.common.BaseEntity;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.spot.SpotEntity;
import com.parkmate.floor.FloorEntity;
import com.parkmate.pricing_rule.PricingRuleEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "area")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaEntity extends BaseEntity {

    @Column(name = "name", length = 100)
    String name;

    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    VehicleType vehicleType;

    @Column(name = "total_spots")
    Integer totalSpots;

    @Column(name = "area_top_left_x")
    Double areaTopLeftX;

    @Column(name = "area_top_left_y")
    Double areaTopLeftY;

    @Column(name = "area_width")
    Double areaWidth;

    @Column(name = "area_height")
    Double areaHeight;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "support_electric_vehicle")
    Boolean supportElectricVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    FloorEntity parkingFloor;

    @OneToMany(mappedBy = "parkingArea", cascade = CascadeType.ALL)
    List<SpotEntity> spots;

    @OneToOne(mappedBy = "parkingArea")
    PricingRuleEntity pricingRuleEntity;
}

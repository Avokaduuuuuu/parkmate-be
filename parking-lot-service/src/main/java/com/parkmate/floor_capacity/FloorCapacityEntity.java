package com.parkmate.floor_capacity;

import com.parkmate.common.BaseEntity;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.floor.FloorEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "floor_capacity")
public class FloorCapacityEntity extends BaseEntity {
    @Column(name = "capacity")
    Integer capacity;

    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    VehicleType vehicleType;

    @Column(name = "support_electric_vehicle")
    Boolean supportElectricVehicle;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    FloorEntity parkingFloor;
}

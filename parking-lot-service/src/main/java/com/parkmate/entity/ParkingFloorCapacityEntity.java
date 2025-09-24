package com.parkmate.entity;

import com.parkmate.entity.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigInteger;
import java.sql.Types;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "parking_floor_capacity")
public class ParkingFloorCapacityEntity extends BaseEntity {
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
    @JoinColumn(name = "parking_floor_id")
    ParkingFloorEntity parkingFloor;
}

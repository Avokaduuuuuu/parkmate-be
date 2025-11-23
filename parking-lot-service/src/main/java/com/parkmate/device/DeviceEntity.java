package com.parkmate.device;

import com.parkmate.common.BaseEntity;
import com.parkmate.device.enums.DeviceStatus;
import com.parkmate.device.enums.DeviceType;
import com.parkmate.parking_lot.ParkingLotEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "device")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceEntity extends BaseEntity {
    @Column(name = "device_id", unique = true)
    String deviceId;

    @Column(name = "device_name")
    String deviceName;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    DeviceType deviceType;

    @Column(name = "partner_id")
    Long partnerId;

    @Column(name = "model")
    String model;

    @Column(name = "serial_number")
    String serialNumber;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    DeviceStatus status;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "last_maintained_at")
    LocalDateTime lastMaintainedAt;

    @Column(name = "notes")
    String notes;

    @ManyToOne
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;
}


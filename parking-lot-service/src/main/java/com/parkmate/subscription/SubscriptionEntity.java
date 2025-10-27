package com.parkmate.subscription;

import com.parkmate.common.BaseEntity;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.subscription.enums.DurationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "subscription")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionEntity extends BaseEntity {

    @Column(name = "name", columnDefinition = "Name of this subscription")
    String name;

    @Column(name = "description", columnDefinition = "Description of the subscription")
    String description;

    @Column(name = "vehicle_type", columnDefinition = "Vehicle Type applied for the subscription")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    VehicleType vehicleType;

    @Column(name = "duration_type", columnDefinition = "Duration of the subscription")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    DurationType durationType;

    @Column(name = "duration_value", columnDefinition = "Duration displayed in number")
    Integer durationValue;

    @Column(name = "price", columnDefinition = "Price of the subscription")
    BigDecimal price;

    @Column(name = "is_active", columnDefinition = "Status of the subscription")
    Boolean isActive;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;
}


package com.parkmate.entity;


import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.entity.enums.RuleScope;
import com.parkmate.entity.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pricing_rule")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class PricingRuleEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    VehicleType vehicleType;

    @Column(name = "rule_name", length = 100)
    String ruleName;

    @Column(name = "base_rate", columnDefinition = "fee after grace period minute")
    Double baseRate;

    @Column(name = "deposit_fee", columnDefinition = "fee for reservation")
    Double depositFee;

    @Column(name = "initial_charge", columnDefinition = "Initial charge when check-in")
    Double initialCharge;

    @Column(name = "initial_duration_minute", columnDefinition = "How long initial charge covers")
    Integer initialDurationMinute;

    @Column(name = "free_minute", columnDefinition = "Time after check-in for free check-out")
    Integer freeMinute;

    @Column(name = "grace_period_minute", columnDefinition = "Charge more fee after a period")
    Integer gracePeriodMinute;

    @Column(name = "rule_scope")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    RuleScope ruleScope;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "valid_from", nullable = false, columnDefinition = "This price rule will be able after this time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime validFrom;

    @Column(name = "valid_until", columnDefinition = "This price rule will be disable after this time")
    LocalDateTime validUntil;

    @Column(name = "created_at")
    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    ParkingLotEntity parkingLot;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_area_id")
    ParkingAreaEntity parkingArea;


    @PrePersist
    private void initializeUUID() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}

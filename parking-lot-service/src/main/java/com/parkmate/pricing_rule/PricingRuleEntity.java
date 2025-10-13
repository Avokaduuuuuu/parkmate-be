package com.parkmate.pricing_rule;


import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.area.AreaEntity;
import com.parkmate.default_pricing_rule.DefaultPricingRuleEntity;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.session.SessionEntity;
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
import java.util.List;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    VehicleType vehicleType;

    @Column(name = "rule_name", length = 100)
    String ruleName;

    @Column(name = "step_rate", columnDefinition = "fee after step period minute")
    Double stepRate;

    @Column(name = "step_minute", columnDefinition = "Charge more fee after a period")
    Integer stepMinute;

    @Column(name = "initial_charge", columnDefinition = "Initial charge when check-in")
    Double initialCharge;

    @Column(name = "initial_duration_minute", columnDefinition = "How long initial charge covers")
    Integer initialDurationMinute;


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
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;

    @OneToMany(mappedBy = "pricingRule")
    List<AreaEntity> areas;

    @OneToMany(mappedBy = "pricingRule")
    List<SessionEntity> sessions;

    @OneToMany(mappedBy = "pricingRule")
    List<DefaultPricingRuleEntity> defaultPricingRules;
}

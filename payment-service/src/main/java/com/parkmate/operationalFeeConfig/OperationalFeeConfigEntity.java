package com.parkmate.operationalFeeConfig;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lot_operational_fee_config")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OperationalFeeConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "price_per_sqm")
    Double pricePerSqm;

    @Column(name = "billing_period_months")
    Integer billingPeriodMonths;

    @Column(name = "description")
    String description;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "valid_from", nullable = false, columnDefinition = "This config will be valid after this time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime validFrom;

    @Column(name = "valid_until", columnDefinition = "This config will be invalid after this time")
    LocalDateTime validUntil;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}

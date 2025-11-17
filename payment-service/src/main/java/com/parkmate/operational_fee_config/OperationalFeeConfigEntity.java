package com.parkmate.operational_fee_config;

import com.parkmate.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lot_operational_fee_config")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OperationalFeeConfigEntity extends BaseEntity {

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
}

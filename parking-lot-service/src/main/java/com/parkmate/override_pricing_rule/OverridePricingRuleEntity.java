package com.parkmate.override_pricing_rule;

import com.parkmate.pricing_rule.PricingRuleEntity;
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
@Builder
@Entity
@Table(name = "override_pricing_rule")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OverridePricingRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    @OneToOne
    @JoinColumn(name = "pricing_rule_id")
    PricingRuleEntity pricingRule;
}

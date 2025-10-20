package com.parkmate.default_pricing_rule;


import com.parkmate.common.enums.VehicleType;
import com.parkmate.default_pricing_rule.id.DefaultPricingRuleId;
import com.parkmate.parking_lot.ParkingLotEntity;
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
@Table(name = "default_pricing_rule")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class DefaultPricingRuleEntity {
    @EmbeddedId
    DefaultPricingRuleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", updatable = false, insertable = false)
    ParkingLotEntity parkingLot;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    PricingRuleEntity pricingRule;

    @Column(name = "created_at")
    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}

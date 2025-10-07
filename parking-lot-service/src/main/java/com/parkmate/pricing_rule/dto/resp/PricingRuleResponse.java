package com.parkmate.pricing_rule.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.pricing_rule.enums.RuleScope;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingRuleResponse {
    Long id;
    VehicleType vehicleType;
    String ruleName;
    Double baseRate;
    Double depositFee;
    Double initialCharge;
    Integer initialDurationMinute;
    Integer freeMinute;
    Integer gracePeriodMinute;
    Boolean isActive;
    RuleScope ruleScope;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime validFrom;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime validUntil;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}

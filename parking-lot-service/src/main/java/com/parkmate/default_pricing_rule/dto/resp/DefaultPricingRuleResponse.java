package com.parkmate.default_pricing_rule.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DefaultPricingRuleResponse {
    Long lotId;
    String lotName;
    PricingRuleResponse pricingRule;
    VehicleType vehicleType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}

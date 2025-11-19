package com.parkmate.pricing_rule.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class PricingRuleSimpleResponse {

    Long id;
    Double initialCharge;
    Integer initialDurationMinute;
    Double stepRate;
    Integer stepMinute;
    Double estimateTotalFee;

}

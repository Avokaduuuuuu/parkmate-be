package com.parkmate.parking_lot.dto.resp;

import com.parkmate.pricing_rule.dto.resp.PricingRuleSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class ParkingLotAvailableReservationSpotResponse {

    Long totalCapacity;
    Long availableCapacity;
    PricingRuleSimpleResponse pricing;
}

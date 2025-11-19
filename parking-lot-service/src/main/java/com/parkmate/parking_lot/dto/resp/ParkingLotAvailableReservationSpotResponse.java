package com.parkmate.parking_lot.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.pricing_rule.dto.resp.PricingRuleSimpleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingLotAvailableReservationSpotResponse {

    Long totalCapacity;
    Long availableCapacity;
    VehicleType vehicleType;
    PricingRuleSimpleResponse pricing;
}

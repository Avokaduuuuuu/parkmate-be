package com.parkmate.default_pricing_rule.dto.req;

import com.parkmate.common.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "DefaultPricingRuleCreateRequest",
        description = "Request payload for creating a default pricing rule assignment for a parking lot"
)
public record DefaultPricingRuleCreateRequest(

        @Schema(
                description = "ID of the parking lot to assign the default pricing rule to",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Lot Id must not be null")
        Long lotId,

        @Schema(
                description = "ID of the pricing rule to set as default for the specified vehicle type",
                example = "5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Pricing Rule Id must not be null")
        Long pricingRuleId,

        @Schema(
                description = """
                        Vehicle type for which this pricing rule will be the default.
                        Each vehicle type can have one default pricing rule per parking lot.
                        
                        **Available Vehicle Types:**
                        • CAR_UP_TO_9_SEATS - Standard sedan or compact car (4-5 passengers)
                        • MOTORBIKE - Motorcycle or scooter
                        • BIKE - Bicycle or e-bike
                        • OTHER - Other vehicle types not listed above
                        
                        The default pricing rule will be automatically applied when:
                        - A new parking session is created without specifying a pricing rule
                        - Quick entry systems need to determine pricing
                        - Mobile app users select this vehicle type
                        """,
                example = "CAR_4_SEATS",
                allowableValues = {"CAR_UP_TO_9_SEATS", "MOTORBIKE", "BIKE", "OTHER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Vehicle Type must not be null")
        VehicleType vehicleType
) {
}
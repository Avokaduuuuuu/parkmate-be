package com.parkmate.subscription.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Schema(description = "Request body for updating an existing subscription package (partial update supported)")
public record SubscriptionUpdateRequest(
        @Schema(
                description = "Updated name of the subscription package",
                example = "Monthly Car Parking - Premium Plus",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String name,

        @Schema(
                description = "Updated description of the subscription package",
                example = "Premium monthly subscription with EV charging support",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String description,

        @Schema(
                description = "Updated price of the subscription in VND",
                example = "1800000.00",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", message = "Base rate must be non-negative")
        BigDecimal price,

        @Schema(
                description = "Whether the subscription package is active and available for purchase",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean isActive
) {
}
package com.parkmate.subscription.dto.req;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.subscription.enums.DurationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Schema(description = "Request body for creating a new subscription package")
public record SubscriptionCreateRequest(
        @Schema(
                description = "Name of the subscription package",
                example = "Monthly Car Parking - Premium",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Subscription's name must not be null")
        @NotEmpty(message = "Subscription's name must not be empty")
        @Length(max = 50, message = "Subscription's name must be within 50 characters")
        String name,

        @Schema(
                description = "Detailed description of the subscription package and its benefits",
                example = "Premium monthly subscription for cars with 24/7 access and reserved spots",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Subscription's description must not be null")
        @NotEmpty(message = "Subscription's description must not be empty")
        @Length(max = 255, message = "Subscription's description must be within 255 characters")
        String description,

        @Schema(
                description = "Type of vehicle this subscription is for",
                example = "CAR_UP_TO_9_SEATS",
                allowableValues = {"BIKE", "MOTORBIKE", "CAR_UP_TO_9_SEATS", "OTHER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Must define vehicle type for subscription")
        VehicleType vehicleType,

        @Schema(
                description = "Duration type of the subscription",
                example = "MONTHLY",
                allowableValues = {"MONTHLY", "QUARTERLY", "YEARLY"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Must define duration type for subscription")
        DurationType durationType,

        @Schema(
                description = "Price of the subscription in VND",
                example = "1500000.00",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Must define the price for the subscription")
        @DecimalMin(value = "0.0", message = "Base rate must be non-negative")
        BigDecimal price,

        @Schema(
                description = "ID of the parking lot this subscription belongs to",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Parking lot's Id owns this subscription must not be null")
        Long lotId
) {
}
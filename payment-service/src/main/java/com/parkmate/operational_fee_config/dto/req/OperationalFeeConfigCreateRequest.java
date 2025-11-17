package com.parkmate.operational_fee_config.dto.req;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record OperationalFeeConfigCreateRequest(
        @NotNull(message = "Price Per Square must not be null")
        Double pricePerSqm,
        @NotNull(message = "Billing Period Months must not be null")
        Integer billingPeriodMonths,
        @NotNull(message = "Description must not be null")
        String description,
        @NotNull(message = "Valid From must not be null")
        LocalDateTime validFrom,
        @NotNull(message = "Valid Until must not be null")
        LocalDateTime validUntil
) {
}

package com.parkmate.client.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CreateOperationalPaymentRequest(
        @NotNull(message = "Lot ID is required")
        Long lotId,

        @NotNull(message = "Partner ID is required")
        Long partnerId,

        @NotNull(message = "Lot area is required")
        @Positive(message = "Lot area must be positive")
        Double lotAreaSqm
) {
}
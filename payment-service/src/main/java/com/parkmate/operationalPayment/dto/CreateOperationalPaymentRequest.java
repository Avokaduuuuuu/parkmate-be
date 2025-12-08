package com.parkmate.operationalPayment.dto;

import com.parkmate.device_payment_item.dto.req.CreateDeviceItemPaymentRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOperationalPaymentRequest(
        @NotNull(message = "Lot ID is required")
        Long lotId,

        @NotNull(message = "Partner ID is required")
        Long partnerId,

        @NotNull(message = "Lot area is required")
        @Positive(message = "Lot area must be positive")
        Double lotAreaSqm,

        List<CreateDeviceItemPaymentRequest> deviceItemPaymentRequests
) {
}
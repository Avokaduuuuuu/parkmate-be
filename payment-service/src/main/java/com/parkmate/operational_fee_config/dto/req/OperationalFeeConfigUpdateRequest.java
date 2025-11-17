package com.parkmate.operational_fee_config.dto.req;

import java.time.LocalDateTime;

public record OperationalFeeConfigUpdateRequest(
        Double pricePerSqm,
        Integer billingPeriodMonths,
        String description,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
}

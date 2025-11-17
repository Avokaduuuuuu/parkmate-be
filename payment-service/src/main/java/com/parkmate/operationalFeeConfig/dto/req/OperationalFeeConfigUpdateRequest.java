package com.parkmate.operationalFeeConfig.dto.req;

import java.time.LocalDateTime;

public record OperationalFeeConfigUpdateRequest(
        Double pricePerSqm,
        Integer billingPeriodMonths,
        String description,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
}

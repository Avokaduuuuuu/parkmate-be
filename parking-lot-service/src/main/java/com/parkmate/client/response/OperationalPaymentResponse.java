package com.parkmate.client.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OperationalPaymentResponse(
        Long id,
        Long lotId,
        Long partnerId,
        Double lotAreaSqm,
        BigDecimal feePerSqm,
        Integer billingPeriodMonths,
        BigDecimal totalFee,
        String paymentStatus,
        String paymentLink,
        String qrCode,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}
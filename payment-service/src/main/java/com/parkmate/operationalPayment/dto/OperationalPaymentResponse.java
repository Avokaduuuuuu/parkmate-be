package com.parkmate.operationalPayment.dto;


import com.parkmate.operationalPayment.enums.PaymentStatus;
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
        PaymentStatus paymentStatus,
        String paymentLink,
        String qrCode,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}
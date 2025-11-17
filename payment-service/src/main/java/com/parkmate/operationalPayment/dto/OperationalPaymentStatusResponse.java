package com.parkmate.operationalPayment.dto;

import com.parkmate.operationalPayment.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OperationalPaymentStatusResponse(
        Long id,
        Long lotId,
        PaymentStatus paymentStatus,
        BigDecimal totalFee,
        LocalDateTime dueDate,
        LocalDateTime paidAt
) {
}
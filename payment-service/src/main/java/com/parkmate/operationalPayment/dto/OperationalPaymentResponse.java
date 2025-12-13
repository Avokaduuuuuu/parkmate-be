package com.parkmate.operationalPayment.dto;


import com.parkmate.device_payment_item.dto.resp.DevicePaymentItemResponse;
import com.parkmate.operationalPayment.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OperationalPaymentResponse(
        Long id,
        Long lotId,
        Long partnerId,
        Double lotAreaSqm,
        BigDecimal feePerSqm,
        Integer billingPeriodMonths,
        BigDecimal areaFee,
        BigDecimal deviceFee,
        BigDecimal totalFee,
        PaymentStatus paymentStatus,
        String paymentLink,
        String qrCode,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        List<DevicePaymentItemResponse> devicePaymentItems
) {
}
package com.parkmate.partnerWithdrawal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.partnerWithdrawal.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponse {
    private UUID id;
    private Long lotId;
    private Long partnerId;
    private List<WithdrawalPeriodSummary> periods;
    private BigDecimal totalGrossRevenue;
    private BigDecimal totalPlatformFee;
    private BigDecimal netAmount;
    private BigDecimal totalAmountReservation;
    private BigDecimal totalAmountSubscription;
    private BigDecimal totalAmountWalkIn;
    private WithdrawalStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    private String failureReason;
    private String description;
    private String externalTransactionId;
}

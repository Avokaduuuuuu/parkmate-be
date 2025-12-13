package com.parkmate.userSubscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.userSubscription.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionResponse {

    private Long id;
    private Long userId;
    private Long vehicleId;
    private String vehicleType;
    private String vehicleLicensePlate;
    private Long subscriptionPackageId;
    private String subscriptionPackageName;
    private Long parkingLotId;
    private String parkingLotName;
    private Long assignedSpotId;
    private String assignedSpotName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private Boolean autoRenew;
    private BigDecimal paidAmount;
    private UUID paymentTransactionId;
    private UserSubscriptionStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelledAt;

    private String cancellationReason;
    private BigDecimal refundAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String qrCode;
    private Long daysRemaining;
    private boolean needRenewalDecision;

}

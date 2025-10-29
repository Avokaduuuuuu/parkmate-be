package com.parkmate.userSubscription.dto;

import com.parkmate.userSubscription.UserSubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request to update a user subscription - all fields are optional")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserSubscriptionRequest {

    @Positive(message = "Assigned spot ID must be positive")
    @Schema(description = "ID of the assigned parking spot", example = "10")
    private Long assignedSpotId;

    @Schema(description = "Subscription start date and time", example = "2024-01-15T08:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Subscription end date and time", example = "2024-02-15T08:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Whether to auto-renew the subscription", example = "true")
    private Boolean autoRenew;

    @Schema(description = "Subscription status", example = "ACTIVE", allowableValues = {"PENDING_PAYMENT", "ACTIVE", "PAUSED", "EXPIRED", "CANCELLED", "REFUNDED"})
    private UserSubscriptionStatus status;

    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    @Schema(description = "Reason for cancellation (max 500 chars)", example = "User requested cancellation")
    private String cancellationReason;

    @Positive(message = "Refund amount must be positive")
    @Schema(description = "Refund amount if applicable", example = "150.00")
    private BigDecimal refundAmount;
}

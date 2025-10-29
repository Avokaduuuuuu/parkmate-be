package com.parkmate.userSubscription.dto;

import com.parkmate.userSubscription.UserSubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Search criteria for filtering user subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionSearchCriteria {

    @Schema(description = "Filter by user ID", example = "1")
    private Long userId;

    @Schema(description = "Filter by vehicle ID", example = "5")
    private Long vehicleId;

    @Schema(description = "Filter by subscription package ID", example = "2")
    private Long subscriptionPackageId;

    @Schema(description = "Filter by parking lot ID", example = "10")
    private Long parkingLotId;

    @Schema(description = "Filter by assigned spot ID", example = "25")
    private Long assignedSpotId;

    @Schema(description = "Filter by subscription status", example = "ACTIVE", allowableValues = {"PENDING_PAYMENT", "ACTIVE", "PAUSED", "EXPIRED", "CANCELLED", "REFUNDED"})
    private UserSubscriptionStatus status;

    @Schema(description = "Filter by auto-renew setting", example = "true")
    private Boolean autoRenew;

    @Schema(description = "Filter subscriptions starting on or after this date", example = "2024-01-01T00:00:00")
    private LocalDateTime startDateFrom;

    @Schema(description = "Filter subscriptions starting on or before this date", example = "2024-12-31T23:59:59")
    private LocalDateTime startDateTo;

    @Schema(description = "Filter subscriptions ending on or after this date", example = "2024-01-01T00:00:00")
    private LocalDateTime endDateFrom;

    @Schema(description = "Filter subscriptions ending on or before this date", example = "2024-12-31T23:59:59")
    private LocalDateTime endDateTo;

    @Schema(description = "Filter by active subscriptions only (current date between start and end date)", example = "true")
    private Boolean isActive;

    @Schema(description = "Filter by expired subscriptions only (current date after end date)", example = "false")
    private Boolean isExpired;

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "10", defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "Sort field", example = "startDate", allowableValues = {"id", "startDate", "endDate", "createdAt", "updatedAt"})
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
}

package com.parkmate.userSubscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request to create a new user subscription")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserSubscriptionRequest {

    private Boolean ownedByMe;

    private Long userId;

    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be positive")
    @Schema(description = "ID of the vehicle for this subscription", example = "1")
    private Long vehicleId;

    @NotNull(message = "Subscription package ID is required")
    @Positive(message = "Subscription package ID must be positive")
    @Schema(description = "ID of the subscription package", example = "1")
    private Long subscriptionPackageId;

    @NotNull(message = "Parking lot ID is required")
    @Positive(message = "Parking lot ID must be positive")
    @Schema(description = "ID of the parking lot", example = "1")
    private Long parkingLotId;

    @Schema(description = "ID of the assigned parking spot (optional)", example = "10")
    private Long assignedSpotId;

    @NotNull(message = "Start date is required")
    @Schema(description = "Subscription start date and time", example = "2025-12-15T00:00:00")
    @Future(message = "End date must be in the future")
    private LocalDateTime startDate;

    @Schema(description = "Whether to auto-renew the subscription", example = "false", defaultValue = "false")
    private Boolean autoRenew;

    @NotNull(message = "Paid amount is required")
    @Positive(message = "Paid amount must be positive")
    @Schema(description = "Amount paid for the subscription", example = "299.99")
    private BigDecimal paidAmount;
}

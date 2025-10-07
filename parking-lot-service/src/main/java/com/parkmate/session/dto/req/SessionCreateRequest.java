package com.parkmate.session.dto.req;

import com.parkmate.session.enums.AuthMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(
        name = "SessionCreateRequest",
        description = "Request payload for creating a new parking session"
)
public record SessionCreateRequest(
        @Schema(
                description = "ID of the user creating the parking session",
                example = "123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long userId,

        @Schema(
                description = "ID of the parking spot being occupied",
                example = "456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long spotId,

        @Schema(
                description = "ID of the vehicle entering the parking lot",
                example = "789",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long vehicleId,

        @Schema(
                description = "License plate number of the vehicle",
                example = "29A-12345",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "License plate must not be null")
        String licensePlate,

        @Schema(
                description = """
                        Authentication method used for entry:
                        • CARD - Physical access card
                        • QR_CODE - QR code scan
                        • LICENSE_PLATE - License plate recognition
                        • MOBILE_APP - Mobile application
                        • MANUAL - Manual entry by staff
                        """,
                example = "CARD",
                allowableValues = {"CARD", "QR_CODE", "LICENSE_PLATE", "MOBILE_APP", "MANUAL"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Auth method must not be null")
        AuthMethod authMethod,

        @Schema(
                description = "Timestamp when the vehicle entered the parking lot",
                example = "2025-10-06T14:30:00",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Entry time must not be null")
        LocalDateTime entryTime,

        @Schema(
                description = "Unique identifier of the access card (required if authMethod is CARD)",
                example = "CARD-UUID-123456",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String cardUUID,

        @Schema(
                description = "ID of the pricing rule to apply for this session",
                example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Pricing rule ID must not be null")
        Long pricingRuleId
) {
}
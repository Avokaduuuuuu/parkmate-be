package com.parkmate.spot.dto.req;

import com.parkmate.spot.enums.SpotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SpotUpdateRequest(
        @Schema(
                description = "Unique identifier/name for the parking spot within the area (e.g., 'A-01', 'B-15')",
                example = "A-01",
                maxLength = 20
        )
        @NotEmpty(message = "Lot name must not be empty")
        String name,
        @Schema(
                description = "Status of this Spot",
                example = "AVAILABLE",
                allowableValues = {"AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "DISABLE"}
        )
        SpotStatus status,
        @Schema(
                description = "The reason if the spot is DISABLE or MAINTENANCE",
                example = "This spot is damaged"
        )
        String blockReason
) {
}

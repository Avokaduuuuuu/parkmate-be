package com.parkmate.spot.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for creating an individual parking spot within an area")
public record SpotCreateRequest(
        @Schema(
                description = "Unique identifier/name for the parking spot within the area (e.g., 'A-01', 'B-15')",
                example = "A-01",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 20
        )
        @NotNull(message = "Lot name must not be null")
        @NotEmpty(message = "Lot name must not be empty")
        String name,

        @Schema(
                description = "X-coordinate of the spot's top-left corner relative to the floor map (in meters or pixels)",
                example = "15.5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Lot top left X must not be null")
        @Positive(message = "Lot top left X must be positive")
        Double spotTopLeftX,

        @Schema(
                description = "Y-coordinate of the spot's top-left corner relative to the floor map (in meters or pixels)",
                example = "25.0",
                requiredMode = Schema.RequiredMode.REQUIRED

        )
        @NotNull(message = "Lot top left Y must not be null")
        @Positive(message = "Lot top left Y must be positive")
        Double spotTopLeftY,

        @Schema(
                description = "Width of the parking spot (in meters). Standard car spot is ~2.5m",
                example = "2.5",
                requiredMode = Schema.RequiredMode.REQUIRED

        )
        @NotNull(message = "Lot width must not be null")
        @Positive(message = "Lot width must be positive")
        Double spotWidth,

        @Schema(
                description = "Length/height of the parking spot (in meters). Standard car spot is ~5.0m",
                example = "5.0",
                requiredMode = Schema.RequiredMode.REQUIRED

        )
        @NotNull(message = "Lot height must not be null")
        @Positive(message = "Lot height must be positive")
        Double spotHeight
) {
}

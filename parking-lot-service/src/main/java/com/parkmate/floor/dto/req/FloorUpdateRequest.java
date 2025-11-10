package com.parkmate.floor.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public record FloorUpdateRequest(
        @Schema(
                description = "Floor number (negative numbers for basement levels, 0 for ground floor, positive for upper floors)",
                example = "1",
                minimum = "-100",
                maximum = "100",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Min(value = -100, message = "Floor number must be a number")
        Integer floorNumber,

        @Schema(
                description = "Display name of the floor",
                example = "Ground Floor",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Length(max = 100, message = "Floor name must not over 100 characters")
        @NotEmpty(message = "Floor name must not be empty")
        String floorName,

        @Schema(
                description = "Top left X coordinate",
                example = "100.1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Double floorTopLeftX,

        @Schema(
                description = "Top left Y coordinate",
                example = "100.1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Double floorTopLeftY,

        @Schema(
                description = "Floor width",
                example = "100.2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "Floor Width must be positive")
        Double floorWidth,

        @Schema(
                description = "Floor height",
                example = "100.12",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "Floor Height must be positive")
        Double floorHeight,

        @Schema(
                description = "Status of floor",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean isActive
) {
}

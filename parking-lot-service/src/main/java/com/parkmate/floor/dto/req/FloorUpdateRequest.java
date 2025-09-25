package com.parkmate.floor.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

public record FloorUpdateRequest(
        @Schema(
                description = "Floor number (negative numbers for basement levels, 0 for ground floor, positive for upper floors)",
                example = "1",
                minimum = "-100",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Min(value = -100, message = "Floor number must be a number")
        Integer floorNumber,

        @Schema(
                description = "Display name of the floor",
                example = "Ground Floor",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Length(max = 100, message = "Floor name must not over 100 characters")
        @NotEmpty(message = "Floor name must not be empty")
        String floorName
) {
}

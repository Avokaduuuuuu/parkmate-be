package com.parkmate.floor.dto.req;

import com.parkmate.floor_capacity.dto.req.FloorCapacityCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Schema(
        name = "ParkingFloorCreateRequest",
        description = "Request payload for creating a new parking floor within a parking lot"
)
public record FloorCreateRequest(

        @Schema(
                description = "Floor number (negative numbers for basement levels, 0 for ground floor, positive for upper floors)",
                example = "1",
                minimum = "-100",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Floor number must not be null")
        @Min(value = -100, message = "Floor number must be a number")
        Integer floorNumber,

        @Schema(
                description = "Display name of the floor",
                example = "Ground Floor",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Floor name must not be null")
        @Length(max = 100, message = "Floor name must not over 100 characters")
        @NotEmpty(message = "Floor name must not be empty")
        String floorName,

        @Schema(
                description = "Top left X coordinate",
                example = "100.1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Top left X must not be null")
        Double floorTopLeftX,

        @Schema(
                description = "Top left Y coordinate",
                example = "100.1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Top left Y must not be null")
        Double floorTopLeftY,

        @Schema(
                description = "Floor width",
                example = "100.2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Floor Width must not be null")
        @Positive(message = "Floor Width must be positive")
        Double floorWidth,

        @Schema(
                description = "Floor height",
                example = "100.12",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Floor Height must not be null")
        @Positive(message = "Floor Height must be positive")
        Double floorHeight,

        @Schema(
                description = "List of capacity configurations for different vehicle types on this floor",
                example = "[{\"capacity\": 50, \"vehicleType\": \"CAR_4_SEATS\", \"supportElectricVehicle\": true}, {\"capacity\": 100, \"vehicleType\": \"MOTORBIKE\", \"supportElectricVehicle\": false}]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Valid
        List<FloorCapacityCreateRequest> capacityRequests

) {
}
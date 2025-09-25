package com.parkmate.parking_lot.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(
        name = "ParkingLotUpdateRequest",
        description = "Request payload for updating a parking lot"
)
public record ParkingLotUpdateRequest(
        @Schema(
                description = "Name of the parking lot",
                example = "Diamond Plaza Parking",
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Parking Lot Name must not be empty")
        String name,

        @Schema(
                description = "Street address of the parking lot",
                example = "34 Le Duan Street",
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Parking Lot Address must not be empty")
        String streetAddress,

        @Schema(
                description = "Ward or district where the parking lot is located",
                example = "Ben Nghe Ward",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Parking Lot Ward must not be empty")
        String ward,

        @Schema(
                description = "City where the parking lot is located",
                example = "Ho Chi Minh City",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "Parking Lot City must not be empty")
        String city,

        @Schema(
                description = "Latitude coordinate of the parking lot location",
                example = "10.7827500",
                minimum = "-90",
                maximum = "90",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        Double latitude,

        @Schema(
                description = "Longitude coordinate of the parking lot location",
                example = "106.6986700",
                minimum = "-180",
                maximum = "180",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        Double longitude,

        @Schema(
                description = "Total number of floors in the parking lot",
                example = "3",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Min(value = 1, message = "Total floors must be at least 1")
        Integer totalFloors,

        @Schema(
                description = "Opening time of the parking lot in HH:mm:ss format",
                example = "06:00:00",
                type = "string",
                format = "time",
                pattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalTime operatingHoursStart,

        @Schema(
                description = "Closing time of the parking lot in HH:mm:ss format",
                example = "23:00:00",
                type = "string",
                format = "time",
                pattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalTime operatingHoursEnd
) {

}

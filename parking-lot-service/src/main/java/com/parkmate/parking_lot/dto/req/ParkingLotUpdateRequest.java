package com.parkmate.parking_lot.dto.req;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
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
                maxLength = 255)
        String name,

        @Schema(
                description = "Street address of the parking lot",
                example = "34 Le Duan Street",
                maxLength = 255
        )
        String streetAddress,

        @Schema(
                description = "Ward or district where the parking lot is located",
                example = "Ben Nghe Ward",
                maxLength = 100
        )
        String ward,

        @Schema(
                description = "City where the parking lot is located",
                example = "Ho Chi Minh City",
                maxLength = 100
        )
        String city,

        @Schema(
                description = "Latitude coordinate of the parking lot location",
                example = "10.7827500",
                minimum = "-90",
                maximum = "90"
        )
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        Double latitude,

        @Schema(
                description = "Longitude coordinate of the parking lot location",
                example = "106.6986700",
                minimum = "-180",
                maximum = "180"
        )
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        Double longitude,

        @Schema(
                description = "Total number of floors in the parking lot",
                example = "3",
                minimum = "1"
        )
        @Min(value = 1, message = "Total floors must be at least 1")
        Integer totalFloors,

        @Schema(
                description = "Opening time of the parking lot in HH:mm:ss format",
                example = "06:00:00",
                type = "string",
                format = "time",
                pattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$"
        )
        LocalTime operatingHoursStart,

        @Schema(
                description = "Closing time of the parking lot in HH:mm:ss format",
                example = "23:00:00",
                type = "string",
                format = "time",
                pattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$"
        )
        LocalTime operatingHoursEnd,

        @Schema(
                description = "Indicate that this parking lot work 24h",
                example = "true",
                type = "boolean"
        )
        Boolean is24Hour,
        @Schema(
                description = """
                Status of the parking lot. Each status represents a specific stage in the parking lot lifecycle:
                
                • UNDER_SURVEY - Initial stage when parking lot is being surveyed and evaluated
                • PREPARING - Parking lot is being prepared for operation (infrastructure setup)
                • REJECTED - Parking lot application/survey has been rejected by administrators
                • PARTNER_CONFIGURATION - Partner is configuring pricing, spots, and other settings
                • ACTIVE_PENDING - Configuration complete, waiting for admin approval to go live
                • ACTIVE - Parking lot is operational and accepting customers
                • INACTIVE - Parking lot is temporarily closed (e.g., off-season, renovation)
                • UNDER_MAINTENANCE - Parking lot is undergoing maintenance or repairs
                • MAP_DENIED - Parking lot location denied on the map system
                
                Note: Not all status transitions are allowed. Check business rules before updating.
                """,
                example = "ACTIVE",
                allowableValues = {"UNDER_SURVEY", "PREPARING", "REJECTED", "PARTNER_CONFIGURATION",
                        "ACTIVE_PENDING", "ACTIVE", "INACTIVE", "UNDER_MAINTENANCE", "MAP_DENIED"}
        )
        ParkingLotStatus status,
        @Schema(
                description = "Reason if parking lot is REJECTED, MAP_DENIED",
                example = "Pricing rule not appropriate",
                type = "string"
        )
        String reason
) {

}

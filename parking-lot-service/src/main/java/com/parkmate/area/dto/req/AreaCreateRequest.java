package com.parkmate.area.dto.req;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.spot.dto.req.SpotCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Request body for creating a new parking area with individual parking spots")
public record AreaCreateRequest(
        @Schema(
                description = "Name of the parking area (e.g., 'Zone A', 'VIP Section')",
                example = "Zone A - Ground Floor",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        @NotNull(message = "Area name must not be null")
        @NotEmpty(message = "Area name must not be empty")
        String name,

        @Schema(
                description = "Type of vehicle this pricing rule applies to",
                example = "CAR_4_SEATS",
                allowableValues = {"BIKE", "MOTORBIKE", "CAR_UP_TO_9_SEATS", "OTHER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Vehicle Type must not be null")
        VehicleType vehicleType,

        @Schema(
                description = "X-coordinate of the area's top-left corner in the floor map (in meters or pixels)",
                example = "10.5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Area Top Left X must not be null")
        @Positive(message = "Area Top Left X must be positive")
        Double areaTopLeftX,

        @Schema(
                description = "Y-coordinate of the area's top-left corner in the floor map (in meters or pixels)",
                example = "20.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Area Top Left Y must not be null")
        @Positive(message = "Area Top Left Y must be positive")
        Double areaTopLeftY,

        @Schema(
                description = "Width of the parking area (in meters or pixels)",
                example = "50.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Area Width must not be null")
        @Positive(message = "Area Width must be positive")
        Double areaWidth,

        @Schema(
                description = "Height of the parking area (in meters or pixels)",
                example = "30.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Area Height must not be null")
        @Positive(message = "Area Height must be positive")
        Double areaHeight,

        @Schema(
                description = "Whether this area supports electric vehicle charging (0 = no, 1 = yes). Should be Boolean instead of Double.",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Must define electric vehicle allowance")
        Boolean supportElectricVehicle,

        @Schema(
                description = "Total spots of an area",
                example = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Must define total spots of an area")
        Integer totalSpots,

        @Schema(
                description = "List of individual parking spots to create within this area. Can be null or empty if spots are created later.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Valid
        List<SpotCreateRequest> spotRequests
) {
}
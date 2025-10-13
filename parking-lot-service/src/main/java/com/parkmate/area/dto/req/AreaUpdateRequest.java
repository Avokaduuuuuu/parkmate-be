package com.parkmate.area.dto.req;

import com.parkmate.area.enums.AreaType;
import com.parkmate.common.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for updating parking area")
public record AreaUpdateRequest(
        @Schema(
                description = "Name of the parking area (e.g., 'Zone A', 'VIP Section')",
                example = "Zone A - Ground Floor",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 100
        )
        @NotEmpty(message = "Area name must not be empty")
        String name,

        @Schema(
                description = "Type of vehicle this pricing rule applies to",
                example = "CAR_4_SEATS",
                allowableValues = {"BIKE", "MOTORBIKE", "CAR_4_SEATS", "CAR_7_SEATS", "CAR_9_SEATS", "OTHER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        VehicleType vehicleType,

        @Schema(
                description = "X-coordinate of the area's top-left corner in the floor map (in meters or pixels)",
                example = "10.5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "Area Top Left X must be positive")
        Double areaTopLeftX,

        @Schema(
                description = "Y-coordinate of the area's top-left corner in the floor map (in meters or pixels)",
                example = "20.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "Area Top Left Y must be positive")
        Double areaTopLeftY,

        @Schema(
                description = "Width of the parking area (in meters or pixels)",
                example = "50.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "Area Width must be positive")
        Double areaWidth,

        @Schema(
                description = "Height of the parking area (in meters or pixels)",
                example = "30.0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "Area Height must be positive")
        Double areaHeight,

        @Schema(
                description = "Whether this area supports electric vehicle charging (0 = no, 1 = yes). Should be Boolean instead of Double.",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Boolean supportElectricVehicle,

        @Schema(
                description = "Whether this area for Reservation or Walk_In customer",
                example = "RESERVED_ONLy",
                allowableValues = {"RESERVED_ONLY", "WALK_IN_ONLY"}
        )
        AreaType areaType
) {
}

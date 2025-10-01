package com.parkmate.lot_capacity.dto.req;

import com.parkmate.common.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LotCapacityCreateRequest(
        @Schema(
                description = "Number of parking spots allocated for this vehicle type",
                example = "50",
                minimum = "0",
                maximum = "10000",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @NotNull(message = "Capacity must not be null")
        @Min(value = 0, message = "Capacity must be non-negative")
        @Max(value = 10000, message = "Capacity cannot exceed 10000")
        Integer capacity,

        @Schema(
                description = "Type of vehicle that can use these parking spots",
                example = "CAR_4_SEATS",
                allowableValues = {"BIKE", "MOTORBIKE", "CAR_4_SEATS", "CAR_7_SEATS", "CAR_9_SEATS", "OTHER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        VehicleType vehicleType,

        @Schema(
                description = "Whether this capacity includes electric vehicle charging support",
                example = "true",
                defaultValue = "false",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean supportElectricVehicle
) {
}

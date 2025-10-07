package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Create vehicle request")
public record CreateVehicleRequest(

        @NotNull(message = "User ID is required")
        @Schema(description = "Owner user ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        Long userId,

        @NotBlank(message = "License plate is required")
        @Size(max = 20, message = "License plate must not exceed 20 characters")
        @Schema(description = "Vehicle license plate number (max 20 chars)", example = "51A-12345", requiredMode = Schema.RequiredMode.REQUIRED)
        String licensePlate,

        @NotBlank(message = "Vehicle brand is required")
        @Size(max = 50, message = "Vehicle brand must not exceed 50 characters")
        @Schema(description = "Vehicle brand/manufacturer (max 50 chars)", example = "Honda", requiredMode = Schema.RequiredMode.REQUIRED)
        String vehicleBrand,

        @NotBlank(message = "Vehicle model is required")
        @Size(max = 50, message = "Vehicle model must not exceed 50 characters")
        @Schema(description = "Vehicle model name (max 50 chars)", example = "Wave Alpha", requiredMode = Schema.RequiredMode.REQUIRED)
        String vehicleModel,

        @NotBlank(message = "Vehicle color is required")
        @Size(max = 30, message = "Vehicle color must not exceed 30 characters")
        @Schema(description = "Vehicle color (max 30 chars)", example = "Red", requiredMode = Schema.RequiredMode.REQUIRED)
        String vehicleColor,

        @NotNull(message = "Vehicle type is required")
        @Schema(description = "Type of vehicle", example = "MOTORBIKE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"MOTORBIKE", "CAR", "BICYCLE", "ELECTRIC_SCOOTER"})
        VehicleType vehicleType,

        @NotNull(message = "Electric status is required")
        @Schema(description = "Whether vehicle is electric", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isElectric,

        @Schema(description = "S3 key of the vehicle registration document image")
        String licenseImage

) {
}

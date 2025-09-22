package com.parkmate.dto.request;

import com.parkmate.entity.enums.VehicleType;
import jakarta.validation.constraints.NotNull;


public record CreateVehicleRequest(

        @NotNull Long userId,
        @NotNull String licensePlate,
        @NotNull String vehicleBrand,
        @NotNull String vehicleModel,
        @NotNull String vehicleColor,
        @NotNull VehicleType vehicleType,
        String licenseImage,
        boolean active
) {
}

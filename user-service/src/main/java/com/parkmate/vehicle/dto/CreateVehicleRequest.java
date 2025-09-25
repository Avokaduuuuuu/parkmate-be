package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;
import jakarta.validation.constraints.NotNull;


public record CreateVehicleRequest(

        @NotNull Long userId,
        @NotNull String licensePlate,
        @NotNull String vehicleBrand,
        @NotNull String vehicleModel,
        @NotNull String vehicleColor,
        @NotNull VehicleType vehicleType,
        @NotNull boolean isElectric,
        String licenseImage
) {
}

package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;

public record UpdateVehicleRequest(
        VehicleType vehicleType,
        String licenseImage,
        String vehicleBrand,
        String vehicleModel,
        String vehicleColor,
        boolean isElectric,
        boolean active
) {
}

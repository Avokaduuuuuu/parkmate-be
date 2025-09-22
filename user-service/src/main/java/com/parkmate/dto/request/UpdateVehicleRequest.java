package com.parkmate.dto.request;

import com.parkmate.entity.enums.VehicleType;

public record UpdateVehicleRequest(
        VehicleType vehicleType,
        String licenseImage,
        String vehicleBrand,
        String vehicleModel,
        String vehicleColor,
        boolean active
) {
}

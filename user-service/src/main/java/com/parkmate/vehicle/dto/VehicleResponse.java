package com.parkmate.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.vehicle.VehicleType;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        BigInteger userId,
        VehicleType vehicleType,
        String licensePlate,
        String vehicleBrand,
        String vehicleModel,
        String vehicleColor,
        boolean isElectric,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        boolean active
) {
}

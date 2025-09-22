package com.parkmate.dto.response;

import com.parkmate.entity.enums.VehicleType;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        BigInteger userId,
        VehicleType vehicleType,
        String licensePlate,
        String vehicleBrand,
        String vehicleModel,
        String vehicleColor,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}

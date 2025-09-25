package com.parkmate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.entity.enums.VehicleType;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
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

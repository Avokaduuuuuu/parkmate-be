package com.parkmate.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.vehicle.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Vehicle response with detailed information")
public class VehicleResponse {
    Long id;
    BigInteger userId;
    VehicleType vehicleType;
    String licensePlate;
    String vehicleBrand;
    String vehicleModel;
    String vehicleColor;
    String vehiclePhotoUrl;
    boolean isElectric;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
    boolean active;

    @Schema(description = "Whether this vehicle is currently in an active or pending reservation", example = "true")
    boolean isInReservation;

    @Schema(description = "Whether this vehicle has an active subscription in the queried parking lot", example = "false")
    boolean hasSubscriptionInThisParkingLot;

    @Schema(description = "Whether the parking lot supports this vehicle type (only returned when parkingLotId is provided in search)", example = "true")
    boolean isSupported;

}



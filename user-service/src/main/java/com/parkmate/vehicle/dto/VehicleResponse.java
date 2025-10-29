package com.parkmate.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.vehicle.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    boolean isInReservation;

    boolean hasSubscriptionInThisParkingLot;

}



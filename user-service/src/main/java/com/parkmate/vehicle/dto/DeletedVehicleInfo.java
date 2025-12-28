package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedVehicleInfo {
    private Long vehicleId;
    private Long userId;
    private String licensePlate;
    private VehicleType vehicleType;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleColor;
    private boolean isElectric;
    private LocalDateTime deletedAt;
}

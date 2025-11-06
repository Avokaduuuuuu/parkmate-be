package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
public class VehicleSimpleResponse {
    Long userId;
    Long vehicleId;
    String licensePlate;
    VehicleType vehicleType;
}

package com.parkmate.dto.resp;

import com.parkmate.entity.enums.VehicleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParkingFloorCapacityResponse {
    Integer capacity;
    VehicleType vehicleType;
    Boolean supportElectricVehicle;
    Boolean isActive;
}

package com.parkmate.lot_capacity.dto.resp;

import com.parkmate.common.enums.VehicleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LotCapacityResponse {
    Integer capacity;
    VehicleType vehicleType;
    Boolean supportElectricVehicle;
    Boolean isActive;
}

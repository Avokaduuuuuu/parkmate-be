package com.parkmate.floor.dto.resp;


import com.parkmate.common.enums.VehicleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloorSubscriptionCapacityResponse {

    Long floorId;
    String floorName;
    Long totalSubscriptionSpots;
    Long availableSubscriptionSpots;
    VehicleType vehicleType;

}

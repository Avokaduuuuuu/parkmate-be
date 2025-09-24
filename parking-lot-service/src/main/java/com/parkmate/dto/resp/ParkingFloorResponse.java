package com.parkmate.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParkingFloorResponse {
    Long id;
    Integer floorNumber;
    String floorName;
    List<ParkingFloorCapacityResponse> parkingFloorCapacity;
}

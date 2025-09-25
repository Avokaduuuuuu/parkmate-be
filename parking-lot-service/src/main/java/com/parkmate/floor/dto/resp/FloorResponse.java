package com.parkmate.floor.dto.resp;

import com.parkmate.floor_capacity.dto.resp.FloorCapacityResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloorResponse {
    Long id;
    Integer floorNumber;
    String floorName;
    Boolean isActive;
    List<FloorCapacityResponse> parkingFloorCapacity;
}

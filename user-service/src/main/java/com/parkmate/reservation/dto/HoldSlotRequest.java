package com.parkmate.reservation.dto;

import com.parkmate.vehicle.VehicleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HoldSlotRequest {

    Long parkingLotId;
    Long userId;
    Long vehicleId;
    LocalDateTime reservedFrom;
    Integer assumedStayMinute;
}
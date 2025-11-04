package com.parkmate.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.vehicle.VehicleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemporaryReservationHold implements Serializable {

    String holdId;
    Long parkingLotId;
    Long userId;
    Long vehicleId;
    VehicleType vehicleType;  // Cached from vehicleId for filtering

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedFrom;

    Integer assumedStayMinute;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime expiresAt;
}
package com.parkmate.reservation.dto;

import com.parkmate.common.enums.ReservationStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class SyncReservationUpdateRequest {

    UUID sessionId;
    ReservationStatus status;
}

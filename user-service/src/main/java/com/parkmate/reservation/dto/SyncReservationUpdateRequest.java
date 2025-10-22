package com.parkmate.reservation.dto;

import com.parkmate.common.enums.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SyncReservationUpdateRequest {

    UUID sessionId;
    BigDecimal totalFee;
    ReservationStatus status;
}

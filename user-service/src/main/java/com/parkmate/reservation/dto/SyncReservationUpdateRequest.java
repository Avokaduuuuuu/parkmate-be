package com.parkmate.reservation.dto;

import com.parkmate.common.enums.ReservationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SyncReservationUpdateRequest {

    UUID sessionId;
    ReservationStatus status;
    BigDecimal totalFee;
    LocalDateTime reservedUntil;
}

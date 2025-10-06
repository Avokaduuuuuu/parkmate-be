package com.parkmate.reservation.dto;

import java.math.BigDecimal;
import java.util.Base64;

public record ReservationResponse(

        Base64 qrCode,
        String reservationId,
        String userId,
        String vehicleId,
        String parkingLotId,
        String parkingSpotId,
        BigDecimal reservationFee,
        String startTime,
        String status

) {
}

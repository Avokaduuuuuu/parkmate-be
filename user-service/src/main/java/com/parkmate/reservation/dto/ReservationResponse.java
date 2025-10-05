package com.parkmate.reservation.dto;

public record ReservationResponse(

        String reservationId,
        String userId,
        String parkingSpotId,
        String vehicleId,
        String startTime,
        String endTime,
        String status

) {
}

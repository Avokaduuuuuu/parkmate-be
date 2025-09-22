package com.parkmate.dto.response;

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

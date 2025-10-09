package com.parkmate.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Reservation response with QR code")
public record ReservationResponse(

        @Schema(description = "Reservation ID", example = "1")
        Long id,

        @Schema(description = "User ID", example = "123")
        String userId,

        @Schema(description = "Vehicle ID", example = "321")
        String vehicleId,

        @Schema(description = "Parking lot ID", example = "456")
        String parkingLotId,

        @Schema(description = "Spot ID", example = "789")
        String spotId,

        @Schema(description = "Reservation fee in VND", example = "10000")
        BigDecimal reservationFee,

        @Schema(description = "Reserved from timestamp", example = "2024-07-01 10:00:00")
        String reservedFrom,

        @Schema(description = "Reservation status", example = "PENDING")
        String status,

        @Schema(description = "Base64 encoded QR code image (PNG format with data URI prefix)", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
        String qrCode

) {
}

package com.parkmate.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigInteger;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Request to update an existing parking reservation")
public record UpdateReservationRequest(

        @Schema(description = "User ID making the reservation", example = "123")
        @NotNull
        Long userId,

        @Schema(description = "Parking lot ID where the reservation is made", example = "456")
        @NotNull
        BigInteger parkingLotId,

        @Schema(description = "Parking lot section ID", example = "789")
        @NotNull
        UUID lotId,

        @Schema(description = "Start time of the reservation", example = "2024-07-01T10:00:00")
        @NotNull
        LocalTime reservedFrom,

        @Schema(description = "End time of the reservation", example = "2024-07-01T12:00:00")
        @NotNull
        LocalTime reservedUntil

) {
}

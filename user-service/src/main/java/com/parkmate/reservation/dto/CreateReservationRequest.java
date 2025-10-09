package com.parkmate.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request to create a new reservation")
@Builder
@Data
public class CreateReservationRequest {

    @Schema(description = "Indicates if the reservation is owned by the requesting user", example = "true")
    boolean ownedByMe;

    @Schema(description = "User ID making the reservation", example = "123")
    @NotNull
    Long userId;

    @Schema(description = "Vehicle ID for the reservation", example = "321")
    @NotNull
    Long vehicleId;

    @Schema(description = "Parking lot ID where the reservation is made", example = "456")
    @NotNull
    Long parkingLotId;

    @Schema(description = "Parking lot section ID", example = "789")
    @NotNull
    Long spotId;

    @Schema(description = "Fee for the reservation", example = "4000")
    BigDecimal reservationFee;

    @Schema(description = "Start time of the reservation", example = "2024-07-01T10:00:00")
    @NotNull
    LocalDateTime reservedFrom;

}


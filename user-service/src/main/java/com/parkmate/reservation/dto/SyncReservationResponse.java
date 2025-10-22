package com.parkmate.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.vehicle.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SyncReservationResponse {
    @Schema(description = "Reservation ID", example = "1")
    Long id;

    @Schema(description = "User ID", example = "123")
    Long userId;

    String fullName;

    @Schema(description = "Vehicle ID", example = "321")
    Long vehicleId;

    VehicleType vehicleType;

    @Schema(description = "", example = "ABC123")
    String licensePlate;

    @Schema(description = "Parking lot ID", example = "456")
    Long parkingLotId;

    @Schema(description = "Spot ID", example = "789")
    Long spotId;

    @Schema(description = "Initial fee in VND", example = "10000")
    BigDecimal initialFee;

    @Schema(description = "Total fee in VND", example = "10000")
    BigDecimal totalFee;

    @Schema(description = "Reserved from timestamp", example = "2024-07-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedFrom;

    @Schema(description = "Reserved until timestamp", example = "2024-07-01 11:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedUntil;

    @Schema(description = "Reservation status", example = "PENDING")
    String status;

    @Schema(description = "Whether this reservation spot was actually used", example = "false")
    Boolean isUsed;

    @Schema(description = "Reservation creation timestamp", example = "2024-07-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @Schema(description = "Reservation last update timestamp", example = "2024-07-01 10:05:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;


}

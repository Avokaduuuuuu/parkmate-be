package com.parkmate.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.vehicle.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Reservation response with QR code")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {
    @Schema(description = "Reservation ID", example = "1")
    Long id;

    @Schema(description = "User ID", example = "123")
    Long userId;

    @Schema(description = "Vehicle ID", example = "321")
    Long vehicleId;

    @Schema(description = "Vehicle plate", example = "36D-49536")
    String vehicleLicensePlate;

    VehicleType vehicleType;

    @Schema(description = "Parking lot ID", example = "456")
    Long parkingLotId;

    @Schema(description = "Parking lot name", example = "Parking Central")
    String parkingLotName;

    @Schema(description = "Initial fee in VND", example = "10000")
    BigDecimal initialFee;

    @Schema(description = "Total fee in VND", example = "10000")
    BigDecimal totalFee;

    @Schema(description = "Refund policy - null if parking lot has no refund policy")
    RefundPolicyDto refundPolicy;

    @Schema(description = "Reserved from timestamp", example = "2024-07-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedFrom;

    @Schema(description = "Reserved until timestamp", example = "2024-07-01 11:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedUntil;

    @Schema(description = "Reservation status", example = "PENDING")
    String status;

    @Schema(description = "Base64 encoded QR code image (PNG format with data URI prefix)", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    String qrCode;

    @Schema(description = "Whether this reservation spot was actually used", example = "false")
    Boolean isUsed;

    @Schema(description = "Reservation creation timestamp", example = "2024-07-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @Schema(description = "Reservation last update timestamp", example = "2024-07-01 10:05:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundPolicyDto {
        @Schema(description = "Minutes before reservation start to get refund", example = "30")
        Integer refundWindowMinutes;

        @Schema(description = "Refund rate/percentage (1.0 = 100%)", example = "1.0")
        Double refundRate;
    }
}




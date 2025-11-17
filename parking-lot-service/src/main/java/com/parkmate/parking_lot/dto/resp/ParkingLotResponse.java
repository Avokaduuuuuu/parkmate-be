package com.parkmate.parking_lot.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParkingLotResponse {
    Long id;
    Long partnerId;
    String name;
    String streetAddress;
    String ward;
    String city;
    Double latitude;
    Double longitude;
    Integer totalFloors;
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime openTime;
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime closeTime;
    Boolean is24Hour;
    ParkingLotStatus status;
    String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    // Operational Payment Fields
    String paymentUrl;           // Payment link URL (existing field)
    String paymentQrCode;        // QR code for payment (base64 encoded)
    Long operationalPaymentId;   // Payment record ID
    String paymentStatus;        // Payment status (PENDING, PAID, CANCELLED, OVERDUE)
    BigDecimal operationalFee;   // Total operational fee amount
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime paymentDueDate;    // Payment due date
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime paymentPaidAt;     // When payment was completed
}

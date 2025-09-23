package com.parkmate.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.entity.enums.ParkingLotStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

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
    Double boundaryTopLeftX;
    Double boundaryTopLeftY;
    Double boundaryWidth;
    Double boundaryHeight;
    ParkingLotStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}

package com.parkmate.parking_lot.dto;

import com.parkmate.parking_lot.enums.ParkingLotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotExcelImportDTO {
    private Long partnerId;
    private String name;
    private String streetAddress;
    private String ward;
    private String city;
    private Double latitude;
    private Double longitude;
    private Integer totalFloors;
    private LocalTime operatingHoursStart;
    private LocalTime operatingHoursEnd;
    private Boolean is24Hour;
    private Double boundaryTopLeftX;
    private Double boundaryTopLeftY;
    private Double boundaryWidth;
    private Double boundaryHeight;
    private ParkingLotStatus status;
    private String reason;
}
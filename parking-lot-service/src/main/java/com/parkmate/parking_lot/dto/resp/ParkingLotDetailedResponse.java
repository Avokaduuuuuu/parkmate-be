package com.parkmate.parking_lot.dto.resp;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.floor.dto.resp.FloorResponse;
import com.parkmate.image.dto.resp.ImageResponse;
import com.parkmate.lot_capacity.dto.resp.LotCapacityResponse;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParkingLotDetailedResponse {
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
    List<FloorResponse> parkingFloors;
    List<PricingRuleResponse> pricingRules;
    List<LotCapacityResponse> lotCapacity;
    List<ImageResponse> images;
}

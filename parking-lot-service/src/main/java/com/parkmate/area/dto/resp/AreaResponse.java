package com.parkmate.area.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.spot.dto.resp.SpotResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaResponse {
    Long id;
    String name;
    VehicleType vehicleType;
    Integer totalSpots;
    Double areaTopLeftX;
    Double areaTopLeftY;
    Double areaWidth;
    Double areaHeight;
    Boolean isActive;
    Boolean supportElectricVehicle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
    List<SpotResponse> spots;
}

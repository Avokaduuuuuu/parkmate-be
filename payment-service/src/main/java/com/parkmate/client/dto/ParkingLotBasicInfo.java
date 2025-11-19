package com.parkmate.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotBasicInfo {
    private Long id;
    private String name;
    private Long partnerId;
}

package com.parkmate.parking_lot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic parking lot information for internal service communication
 * Used by payment-service for withdrawal period calculations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotBasicInfo {
    private Long id;
    private String name;
    private Long partnerId;
}

package com.parkmate.parking_lot.dto.resp;

import com.parkmate.client.request.CreateDeviceItemPaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<CreateDeviceItemPaymentRequest> deviceItemPaymentRequests;

}

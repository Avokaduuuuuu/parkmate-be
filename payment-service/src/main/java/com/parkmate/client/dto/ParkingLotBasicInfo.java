package com.parkmate.client.dto;

import com.parkmate.device_payment_item.dto.req.CreateDeviceItemPaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

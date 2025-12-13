package com.parkmate.client.request;

import com.parkmate.device.enums.DeviceType;

public record CreateDeviceItemPaymentRequest(
        DeviceType deviceType,
        Integer totalDevice
) {
}

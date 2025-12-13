package com.parkmate.device_payment_item.dto.req;

import com.parkmate.common.enums.DeviceType;

public record CreateDeviceItemPaymentRequest(
        DeviceType deviceType,
        Integer totalDevice
) {
}

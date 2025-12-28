package com.parkmate.device.dto.req;

import com.parkmate.device.enums.DeviceStatus;

public record DeviceUpdateRequest(
        String deviceId,
        String deviceName,
        String model,
        String serialNumber,
        DeviceStatus deviceStatus,
        String notes
) {
}

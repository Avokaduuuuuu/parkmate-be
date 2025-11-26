package com.parkmate.device.dto.req;

import com.parkmate.device.enums.DeviceStatus;
import com.parkmate.device.enums.DeviceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DeviceUpdateRequest(
        String deviceId,
        String deviceName,
        String model,
        String serialNumber,
        DeviceStatus deviceStatus,
        String notes
) {
}

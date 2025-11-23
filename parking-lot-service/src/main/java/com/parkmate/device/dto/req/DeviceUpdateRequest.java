package com.parkmate.device.dto.req;

import com.parkmate.device.enums.DeviceStatus;
import com.parkmate.device.enums.DeviceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DeviceUpdateRequest(
        @NotEmpty(message = "Device Id must not be empty")
        String deviceId,
        @NotEmpty(message = "Device Name must not be empty")
        String deviceName,
        String model,
        String serialNumber,
        DeviceStatus deviceStatus,
        Boolean isActive
) {
}

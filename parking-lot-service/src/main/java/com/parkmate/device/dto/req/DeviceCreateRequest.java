package com.parkmate.device.dto.req;

import com.parkmate.device.enums.DeviceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DeviceCreateRequest(
        @NotNull(message = "Device Id must not be null")
        @NotEmpty(message = "Device Id must not be empty")
        String deviceId,

        @NotNull(message = "Device Name must not be null")
        @NotEmpty(message = "Device Name must not be empty")
        String deviceName,
        DeviceType deviceType,
        Long partnerId,
        String model,
        String serialNumber
) {
}

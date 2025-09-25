package com.parkmate.mobileDevice.dto;

import com.parkmate.mobileDevice.DeviceOs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update mobile device information")
public record UpdateMobileDeviceRequest(
        @Schema(description = "Device display name", example = "iPhone 15 Pro Max")
        @Size(max = 100, message = "Device name cannot exceed 100 characters")
        String deviceName,

        @Schema(description = "Operating system of the device", example = "IOS")
        DeviceOs deviceOs,

        @Schema(description = "Push notification token", example = "updated-push-token-xyz789")
        @Size(max = 500, message = "Push token cannot exceed 500 characters")
        String pushToken,

        @Schema(description = "Device active status", example = "true")
        Boolean isActive
) {
}

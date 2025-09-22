package com.parkmate.dto.request;

import com.parkmate.entity.enums.DeviceOs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new mobile device for user")
public record CreateMobileDeviceRequest(
        @Schema(description = "User ID who owns the device", example = "123")
        @NotNull(message = "User ID is required")
        Long userId,

        @Schema(description = "Unique device identifier", example = "DEVICE-12345-ABCDE")
        @NotBlank(message = "Device ID is required")
        @Size(max = 100, message = "Device ID cannot exceed 100 characters")
        String deviceId,

        @Schema(description = "Device display name", example = "iPhone 15 Pro")
        @Size(max = 100, message = "Device name cannot exceed 100 characters")
        String deviceName,

        @Schema(description = "Operating system of the device", example = "IOS")
        @NotNull(message = "Device OS is required")
        DeviceOs deviceOs,

        @Schema(description = "Push notification token", example = "push-token-xyz123")
        @Size(max = 500, message = "Push token cannot exceed 500 characters")
        String pushToken
) {
}

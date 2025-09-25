package com.parkmate.mobileDevice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.mobileDevice.DeviceOs;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;


public record MobileDeviceResponse(
        @Schema(description = "Device unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "User ID who owns the device", example = "123")
        Long userId,

        @Schema(description = "User information", implementation = UserInfo.class)
        UserInfo user,

        @Schema(description = "Unique device identifier", example = "DEVICE-12345-ABCDE")
        String deviceId,

        @Schema(description = "Device display name", example = "iPhone 15 Pro")
        String deviceName,

        @Schema(description = "Operating system of the device", example = "IOS")
        DeviceOs deviceOs,

        @Schema(description = "Push notification token", example = "push-token-xyz123")
        String pushToken,

        @Schema(description = "Device active status", example = "true")
        Boolean isActive,

        @Schema(description = "Last time device was active", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastActiveAt,

        @Schema(description = "Device creation timestamp", example = "2024-01-10T09:15:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    @Schema(description = "Basic user information")
    public record UserInfo(
            @Schema(description = "User ID", example = "123")
            Long id,

            @Schema(description = "User full name", example = "John Doe")
            String fullName
    ) {
    }

}

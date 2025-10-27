package com.parkmate.mobileDevice.dto;

import com.parkmate.mobileDevice.DeviceOs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to create a new mobile device for user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMobileDeviceRequest {

    private Boolean ownedByMe;

    @Schema(description = "User ID who owns the device", example = "123")
    private Long userId;

    @Schema(description = "Unique device identifier", example = "DEVICE-12345-ABCDE")
    @NotBlank(message = "Device ID is required")
    @Size(max = 100, message = "Device ID cannot exceed 100 characters")
    private String deviceId;

    @Schema(description = "Device display name", example = "iPhone 15 Pro")
    @Size(max = 100, message = "Device name cannot exceed 100 characters")
    private String deviceName;

    @Schema(description = "Operating system of the device", example = "IOS")
    @NotNull(message = "Device OS is required")
    private DeviceOs deviceOs;

    @Schema(description = "FCM/APNS push notification token", example = "push-token-xyz123")
    @NotBlank(message = "Push token is required")
    @Size(max = 500, message = "Push token cannot exceed 500 characters")
    private String pushToken;
}

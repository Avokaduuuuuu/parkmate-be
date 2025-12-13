package com.parkmate.device_fee_config.dto.req;

import com.parkmate.common.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "DeviceFeeConfigCreateRequest",
        description = "Request payload for creating a new device operational fee configuration"
)
public record DeviceFeeConfigCreateRequest(
        @Schema(
                description = """
                        Type of IoT device for which the operational fee is being configured.
                        Available device types in the ParkMate system:
                        - ULTRASONIC_SENSOR: Ultrasonic sensors mounted in parking spots for vehicle detection
                        - NFC_READER: NFC/RFID card readers for contactless authentication at entry/exit points
                        - BLE_SCANNER: Bluetooth Low Energy scanners for proximity-based vehicle detection
                        - CAMERA: IP cameras for license plate recognition and surveillance
                        - BARRIER_CONTROLLER: Automated barrier gate controllers for entry/exit management
                        - DISPLAY_BOARD: Electronic display boards showing availability and pricing information
                        """,
                example = "ULTRASONIC_SENSOR",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"ULTRASONIC_SENSOR", "NFC_READER", "BLE_SCANNER", "CAMERA", "BARRIER_CONTROLLER", "DISPLAY_BOARD"}
        )
        @NotNull(message = "Device Type must not be null")
        DeviceType deviceType,

        @Schema(
                description = """
                        Operational fee amount for the device in the system's base currency (VND).
                        This represents the cost associated with operating, maintaining, or licensing this device type.
                        The fee should be a positive decimal value with up to 2 decimal places.
                        
                        Examples:
                        - 150000.00 VND for ultrasonic sensors (monthly operational cost)
                        - 500000.00 VND for cameras (including cloud storage and processing)
                        - 75000.00 VND for NFC readers (card processing fees)
                        """,
                example = "150000.00",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Device Fee must not be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "Device Fee must be greater than or equal to 0")
        BigDecimal deviceFee,

        @Schema(
                description = """
                        Start date and time when this fee configuration becomes valid and active.
                        If not specified, the configuration is valid from the time of creation.
                        Use this to schedule future fee changes or set up historical records.
                        Format: ISO 8601 date-time (yyyy-MM-dd'T'HH:mm:ss)
                        """,
                example = "2025-01-01T00:00:00",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @NotNull(message = "Valid From must not be null")
        LocalDateTime validFrom,

        @Schema(
                description = """
                        End date and time when this fee configuration expires and is no longer valid.
                        If not specified, the configuration remains valid indefinitely.
                        Use this for temporary fee adjustments, promotional periods, or scheduled fee updates.
                        Must be after validFrom if both are specified.
                        Format: ISO 8601 date-time (yyyy-MM-dd'T'HH:mm:ss)
                        """,
                example = "2025-12-31T23:59:59",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        LocalDateTime validUntil,

        @Schema(
                description = """
                        Optional description or notes about this fee configuration.
                        Use this field to document:
                        - Reason for the fee amount (e.g., "Increased due to supplier price change")
                        - Special conditions or context (e.g., "Promotional rate for Q1 2025")
                        - Administrative notes (e.g., "Approved by finance on 2024-12-01")
                        - Contract or vendor references
                        Maximum length: 500 characters
                        """,
                example = "Annual operational fee for ultrasonic sensors - includes maintenance and calibration costs",
                maxLength = 500,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
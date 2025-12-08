package com.parkmate.device_fee_config.dto.req;

import com.parkmate.common.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
        name = "DeviceFeeConfigUpdateRequest",
        description = "Request payload for updating an existing device operational fee configuration. All fields are optional - only include fields you want to update."
)
public record DeviceFeeConfigUpdateRequest(
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
                        
                        Note: Changing device type should be done carefully as it affects the configuration's purpose.
                        """,
                example = "CAMERA",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                allowableValues = {"ULTRASONIC_SENSOR", "NFC_READER", "BLE_SCANNER", "CAMERA", "BARRIER_CONTROLLER", "DISPLAY_BOARD"}
        )
        DeviceType deviceType,

        @Schema(
                description = """
                        Operational fee amount for the device in the system's base currency (VND).
                        This represents the cost associated with operating, maintaining, or licensing this device type.
                        The fee should be a positive decimal value with up to 2 decimal places.
                        
                        Common update scenarios:
                        - Annual inflation adjustment (e.g., increase by 3-5%)
                        - Vendor contract renewal with new pricing
                        - Technology upgrade affecting operational costs
                        - Promotional or discounted rates for limited periods
                        
                        Examples:
                        - 175000.00 VND (updated from 150000.00 due to inflation)
                        - 450000.00 VND (reduced from 500000.00 with new supplier)
                        """,
                example = "175000.00",
                minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", inclusive = true, message = "Device Fee must be greater than or equal to 0")
        BigDecimal deviceFee,

        @Schema(
                description = """
                        Start date and time when this fee configuration becomes valid and active.
                        Updates to this field allow you to:
                        - Postpone or advance the effective date of a configuration
                        - Align validity periods with fiscal years or contract terms
                        - Correct data entry errors
                        
                        Format: ISO 8601 date-time (yyyy-MM-dd'T'HH:mm:ss)
                        """,
                example = "2025-01-15T00:00:00",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        LocalDateTime validFrom,

        @Schema(
                description = """
                        End date and time when this fee configuration expires and is no longer valid.
                        Updates to this field allow you to:
                        - Extend or shorten the validity period
                        - Set an expiration date for previously indefinite configurations
                        - Remove expiration (set to null) to make configuration valid indefinitely
                        
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
                        - Reason for updates (e.g., "Fee increased due to new regulatory requirements")
                        - Change history (e.g., "Updated on 2025-01-10: Adjusted for contract renewal")
                        - Special conditions or context
                        - Administrative notes and approvals
                        - Contract or vendor references
                        
                        Maximum length: 500 characters
                        """,
                example = "Updated operational fee - reflects new maintenance contract signed on 2025-01-05",
                maxLength = 500,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(
                description = """
                        Boolean flag to activate or deactivate this fee configuration.
                        
                        **CRITICAL BUSINESS RULE:**
                        - Setting this to TRUE will AUTOMATICALLY DEACTIVATE all other configurations for the same device type
                        - Only ONE configuration per device type can be active at any given time
                        - The activation process is atomic and uses database-level constraints to prevent race conditions
                        
                        **Use Cases:**
                        - Set to true: Activate this configuration (making it the current operational fee for the device type)
                        - Set to false: Deactivate this configuration (useful for suspending fees temporarily)
                        - Leave as null: Keep the current activation status unchanged
                        
                        **Activation Flow Example:**
                        1. You have Config A (CAMERA, active=true, fee=500000)
                        2. You create Config B (CAMERA, active=false, fee=450000)
                        3. You update Config B with isActive=true
                        4. System automatically sets Config A to active=false
                        5. Config B becomes the new active configuration for CAMERA devices
                        
                        **Important Notes:**
                        - This operation cannot be undone automatically
                        - Ensure the new fee amount and validity period are correct before activation
                        - The previous active configuration remains in the database for historical records
                        - Consider checking which configuration is currently active before making changes
                        """,
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean isActive
) {
}
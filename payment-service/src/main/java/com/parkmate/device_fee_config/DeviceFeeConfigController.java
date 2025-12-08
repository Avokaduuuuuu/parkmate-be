package com.parkmate.device_fee_config;

import com.parkmate.common.ApiResponse;
import com.parkmate.device_fee_config.dto.req.DeviceFeeConfigCreateRequest;
import com.parkmate.device_fee_config.dto.req.DeviceFeeConfigUpdateRequest;
import com.parkmate.device_fee_config.dto.resp.DeviceFeeConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/operational-fee-configs")
@RequiredArgsConstructor
@Tag(name = "Device Fee Configuration API", description = "API for managing operational fee configurations for IoT devices in the ParkMate parking system")
public class DeviceFeeConfigController {

    private final DeviceFeeConfigService deviceFeeConfigService;

    @GetMapping
    @Operation(
            summary = "Get all device fee configurations with filtering and pagination",
            description = """
                    Retrieve a paginated list of device operational fee configurations with optional filtering and sorting.
                    
                    **Query Parameters:**
                    - `deviceType` (optional): Filter by specific device type
                      * ULTRASONIC_SENSOR - Sensors for vehicle detection in parking spots
                      * NFC_READER - NFC card readers for entry/exit authentication
                      * BLE_SCANNER - Bluetooth Low Energy scanners for proximity detection
                      * CAMERA - Cameras for license plate recognition
                      * BARRIER_CONTROLLER - Controllers for entry/exit barriers
                      * DISPLAY_BOARD - Electronic display boards for information
                    - `isActive` (optional): Filter by active status (true/false)
                    - `validFromStart` (optional): Filter configurations valid from this date onwards
                    - `validFromEnd` (optional): Filter configurations valid from before this date
                    - `validUntilStart` (optional): Filter configurations expiring from this date onwards
                    - `validUntilEnd` (optional): Filter configurations expiring before this date
                    - `page` (optional): Page number (default: 0, zero-indexed)
                    - `size` (optional): Page size (default: 20, max: 100)
                    - `sortBy` (optional): Sort field (default: createdAt) - Available: id, deviceType, deviceFee, validFrom, validUntil, createdAt, isActive
                    - `sortOrder` (optional): Sort direction ASC/DESC (default: DESC)
                    
                    **Use Cases:**
                    - View all device operational fees for budget planning
                    - Find active fee configurations for specific device types
                    - Track historical fee changes over time
                    - Identify expiring configurations that need renewal
                    
                    **Returns:** Paginated list of device fee configurations matching the filter criteria
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved device fee configurations",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> findAll(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page (max: 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC/DESC)", example = "DESC")
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,

            DeviceFeeConfigFilterParams params
    ) {
        Page<DeviceFeeConfigResponse> result = deviceFeeConfigService.fetchDeviceFeeConfigs(
                page, size, sortBy, sortOrder, params
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Device fee configurations retrieved successfully",
                        result
                ));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get device fee configuration by ID",
            description = """
                    Retrieve detailed information of a specific device fee configuration by its unique identifier.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the device fee configuration
                    
                    **Returns:** Complete device fee configuration details including:
                    - Device type and operational fee amount
                    - Validity period (validFrom and validUntil dates)
                    - Active status
                    - Description and notes
                    - Creation and last update timestamps
                    
                    **Use Cases:**
                    - View details of a specific fee configuration
                    - Verify current fee for a device type
                    - Check validity period and expiration date
                    - Review configuration history
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved device fee configuration",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device fee configuration not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> findById(
            @Parameter(description = "ID of the device fee configuration to retrieve", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        DeviceFeeConfigResponse result = deviceFeeConfigService.fetchyById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Device fee configuration retrieved successfully",
                        result
                ));
    }

    @PostMapping
    @Operation(
            summary = "Create a new device fee configuration",
            description = """
                    Create a new operational fee configuration for a specific IoT device type.
                    
                    **Request Body Fields:**
                    - `deviceType` (required): Type of IoT device
                      * ULTRASONIC_SENSOR - Ultrasonic sensors for vehicle detection
                      * NFC_READER - NFC card readers for authentication
                      * BLE_SCANNER - Bluetooth scanners for proximity detection
                      * CAMERA - License plate recognition cameras
                      * BARRIER_CONTROLLER - Entry/exit barrier controllers
                      * DISPLAY_BOARD - Electronic information display boards
                    - `deviceFee` (required): Operational fee amount (must be >= 0)
                    - `validFrom` (optional): Start date/time when this configuration becomes valid
                    - `validUntil` (optional): End date/time when this configuration expires
                    - `description` (optional): Additional notes or description (max 500 characters)
                    
                    **Business Rules:**
                    - New configurations are created with isActive = false by default
                    - Only one configuration per device type can be active at a time
                    - Use the PUT endpoint to activate a configuration
                    - Device fee must be a non-negative decimal value
                    
                    **Use Cases:**
                    - Define operational costs for new device types
                    - Create future-dated fee configurations
                    - Set up temporary promotional pricing
                    - Document historical fee structures
                    
                    **Returns:** Created device fee configuration with assigned ID and initial status
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Device fee configuration created successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device fee configuration details to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceFeeConfigCreateRequest.class)
                    )
            )
            @RequestBody @Valid DeviceFeeConfigCreateRequest request
    ) {
        DeviceFeeConfigResponse result = deviceFeeConfigService.addDeviceFeeConfig(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Device fee configuration created successfully",
                        result
                ));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing device fee configuration",
            description = """
                    Update device fee configuration information. All fields are optional - only include fields you want to update.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the device fee configuration to update
                    
                    **Request Body Fields (all optional):**
                    - `deviceType`: Type of IoT device (ULTRASONIC_SENSOR, NFC_READER, BLE_SCANNER, CAMERA, BARRIER_CONTROLLER, DISPLAY_BOARD)
                    - `deviceFee`: Operational fee amount (must be >= 0)
                    - `validFrom`: Start date/time when this configuration becomes valid
                    - `validUntil`: End date/time when this configuration expires
                    - `description`: Additional notes or description (max 500 characters)
                    - `isActive`: Boolean to activate/deactivate this configuration
                    
                    **Important Notes on Active Status:**
                    - Setting `isActive = true` will automatically deactivate all other configurations for the same device type
                    - Only ONE configuration per device type can be active at any time
                    - The system uses native SQL query to ensure atomic activation (prevents race conditions)
                    - Setting `isActive = false` simply deactivates the configuration
                    
                    **Use Cases:**
                    - Update fee amounts for inflation or policy changes
                    - Extend or modify validity periods
                    - Activate a new fee configuration (deactivating the previous one)
                    - Temporarily deactivate a configuration
                    - Add or update description notes
                    
                    **Business Rules:**
                    - Cannot have multiple active configurations for the same device type
                    - Activating a configuration is an atomic operation
                    - All field updates are optional and partial updates are supported
                    
                    **Returns:** Updated device fee configuration information
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Device fee configuration updated successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device fee configuration not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> update(
            @Parameter(description = "ID of the device fee configuration to update", required = true, example = "1")
            @PathVariable("id") Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device fee configuration fields to update (all optional)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceFeeConfigUpdateRequest.class)
                    )
            )
            @RequestBody @Valid DeviceFeeConfigUpdateRequest request
    ) {
        DeviceFeeConfigResponse result = deviceFeeConfigService.updateDeviceFeeConfig(request, id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Device fee configuration updated successfully",
                        result
                ));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a device fee configuration",
            description = """
                    Permanently delete a device fee configuration by its unique identifier.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the device fee configuration to delete
                    
                    **Behavior:**
                    - Permanently removes the configuration from the database
                    - This is a hard delete operation (not a soft delete/deactivation)
                    - Cannot be undone - deleted configurations cannot be recovered
                    - Associated historical records may be affected
                    
                    **Important Warnings:**
                    - ⚠️ Use with caution - this operation is irreversible
                    - Consider deactivating (isActive = false) instead of deleting for historical records
                    - Ensure no active dependencies before deletion
                    - May affect financial reports that reference this configuration
                    
                    **Use Cases:**
                    - Remove test or incorrect configurations
                    - Clean up duplicate entries
                    - Remove obsolete device types no longer in use
                    
                    **Best Practice:**
                    - For operational configurations, prefer setting isActive = false via PUT /operational-fee-configs/{id}
                    - Use DELETE only for removing erroneous or test data
                    
                    **Returns:** HTTP 200 with success message
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Device fee configuration deleted successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Device fee configuration not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions to delete configurations",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "ID of the device fee configuration to delete", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        deviceFeeConfigService.deleteDeviceFeeConfig(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Device fee configuration deleted successfully"
                ));
    }
}
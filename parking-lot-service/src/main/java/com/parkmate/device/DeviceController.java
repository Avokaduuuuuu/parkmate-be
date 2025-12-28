package com.parkmate.device;

import com.parkmate.common.ApiResponse;
import com.parkmate.device.dto.req.DeviceCreateRequest;
import com.parkmate.device.dto.req.DeviceUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking-service/devices")
@RequiredArgsConstructor
@Tag(name = "Device API", description = "API requests for Device Management")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(
            summary = "Get all devices with pagination",
            description = """
                    Retrieve all registered devices with pagination and sorting support.
                    
                    **Query Parameters:**
                    - `page` (optional): Page number (default: 0)
                    - `size` (optional): Page size (default: 10)
                    - `sortBy` (optional): Sort field (default: id) 
                      * Available fields: id, deviceId, deviceName, deviceType, status, createdAt, updatedAt
                    - `sortOrder` (optional): Sort direction ASC/DESC (default: ASC)
                    
                    **Returns:** Paginated list of devices with complete information including:
                    - Device identification (deviceId, deviceName)
                    - Device type (CAMERA, NFC_READER, BLE_SCANNER, etc.)
                    - Associated parking lot and partner
                    - Hardware information (model, serial number)
                    - Status and activity state
                    - Location details
                    """
    )
    public ResponseEntity<?> getAllDevices(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,

            DeviceFilterParams filterParams
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "All devices retrieved successfully",
                                deviceService.getDevices(page, size, sortBy, sortOrder, filterParams)
                        )
                );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get device by ID",
            description = """
                    Retrieve detailed information of a specific device by its ID.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the device
                    
                    **Returns:** Complete device details including:
                    - Device identification (deviceId, deviceName, type)
                    - Associated parking lot and partner information
                    - Physical location (floor, area description)
                    - Hardware details (model, serial number)
                    - Current status and activity state
                    - Maintenance information
                    - Creation and update timestamps
                    - Notes and additional information
                    """
    )
    public ResponseEntity<?> getDeviceById(
            @Parameter(description = "ID of the device to retrieve", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Device retrieved successfully",
                                deviceService.getDeviceById(id)
                        )
                );
    }

    @PostMapping("/lot/{lotId}")
    @Operation(
            summary = "Register a new device for a parking lot",
            description = """
                    Register a new device for a specific parking lot.
                    
                    **Path Parameters:**
                    - `lotId` (required): The ID of the parking lot this device belongs to
                    
                    **Request Body Fields:**
                    - `deviceId` (required): Unique device identifier (e.g., "CAM-ENTRY-01", "NFC-EXIT-02")
                      * Must be unique across all devices
                      * Max 50 characters
                    - `deviceName` (required): User-friendly device name (max 255 characters)
                    - `deviceType` (required): Type of device
                      * ULTRASONIC_SENSOR - Spot occupancy detection
                      * NFC_READER - Entry/exit card reader
                      * BLE_SCANNER - BLE proximity detection
                      * CAMERA - License plate recognition
                      * BARRIER_CONTROLLER - Gate control
                      * DISPLAY_BOARD - Digital signage
                    - `partnerId` (required): ID of the partner who owns this device
                    - `floorId` (optional): ID of the floor where device is located
                    - `locationDescription` (optional): Physical location description (e.g., "Entry gate A")
                    - `model` (optional): Device model/brand (max 100 characters)
                    - `serialNumber` (optional): Manufacturer serial number (max 100 characters)
                    - `notes` (optional): Installation notes, issues, etc.
                    
                    **Business Rules:**
                    - Device ID must be unique
                    - Parking lot must exist
                    - Initial status will be set to PENDING
                    - Initial active state will be false
                    - Partner must have access to the parking lot
                    
                    **Returns:** Created device with assigned database ID and initial status
                    """
    )
    public ResponseEntity<?> createDevices(
            @Parameter(description = "ID of the parking lot", required = true, example = "1")
            @PathVariable("lotId") Long lotId,

            @RequestBody @Valid List<DeviceCreateRequest> request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Device registered successfully",
                                deviceService.createDevices(lotId, request)
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update device information",
            description = """
                    Update device information. All fields are optional - only include fields you want to update.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the device to update
                    
                    **Request Body Fields (all optional):**
                    - `deviceId`: Unique device identifier (max 50 characters)
                      * Must be unique if changed
                    - `deviceName`: User-friendly device name (max 255 characters)
                    - `deviceStatus`: Update device status
                      * PENDING - Registered but not activated
                      * ACTIVE - Online and working
                      * OFFLINE - Not responding
                      * MAINTENANCE - Under maintenance
                      * FAULTY - Hardware issue detected
                      * DEACTIVATED - Disabled by partner
                    - `floorId`: ID of the floor where device is located
                    - `locationDescription`: Physical location description
                    - `model`: Device model/brand (max 100 characters)
                    - `serialNumber`: Manufacturer serial number (max 100 characters)
                    - `isActive`: Boolean indicating if device is active
                    - `lastMaintainedAt`: Timestamp of last maintenance
                    - `notes`: Installation notes, issues, etc.
                    
                    **Important Notes:**
                    - Changing deviceId requires uniqueness validation
                    - Status changes should follow operational workflow
                    - Setting status to MAINTENANCE or FAULTY may trigger alerts
                    - Deactivated devices won't be used in parking operations
                    
                    **Returns:** Updated device information
                    """
    )
    public ResponseEntity<?> updateDevice(
            @Parameter(description = "ID of the device to update", required = true, example = "1")
            @PathVariable("id") Long id,

            @RequestBody @Valid DeviceUpdateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Device updated successfully",
                                deviceService.updateDevice(id, request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a device",
            description = """
                    Permanently delete a device from the system.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the device to delete
                    
                    **Behavior:**
                    - Permanently removes device record from database
                    - Cannot be undone
                    - Should only be used for devices that are no longer in use
                    
                    **Important Considerations:**
                    - Ensure device is not currently in use for operations
                    - Consider setting status to DEACTIVATED instead for temporary removal
                    - This action cannot be reversed
                    - Historical data referencing this device may be affected
                    
                    **Use Cases:**
                    - Device is permanently removed from parking lot
                    - Device is broken beyond repair and replaced
                    - Cleaning up test/demo devices
                    
                    **Returns:** Confirmation message of deletion
                    """
    )
    public ResponseEntity<?> deleteDevice(
            @Parameter(description = "ID of the device to delete", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        deviceService.deleteDevice(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Device deleted successfully",
                                null
                        )
                );
    }



    @GetMapping("/count")
    @Operation(
            summary = "Count total devices",
            description = """
                    Get the total count of devices in the system.
                    
                    **Query Parameters (all optional):**
                    - `lotId`: Count devices for specific parking lot
                    - `deviceType`: Count devices of specific type
                    - `status`: Count devices with specific status
                    - `isActive`: Count only active/inactive devices
                    
                    **Returns:** Total count of devices matching criteria
                    """
    )
    public ResponseEntity<?> countDevices(
    ) {
        // This would need to be implemented in the service
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Device count retrieved successfully",
                                deviceService.countDevices()
                        )
                );
    }

    @GetMapping("/types")
    @Operation(
            summary = "Get all available device types",
            description = """
                    Retrieve list of all supported device types in the system.
                    
                    **Returns:** Array of device types with descriptions:
                    - ULTRASONIC_SENSOR - Spot occupancy detection
                    - NFC_READER - Entry/exit card reader
                    - BLE_SCANNER - BLE proximity detection
                    - CAMERA - License plate recognition
                    - BARRIER_CONTROLLER - Gate control
                    - DISPLAY_BOARD - Digital signage
                    """
    )
    public ResponseEntity<?> getDeviceTypes() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Device types retrieved successfully",
                                new String[]{
                                        "ULTRASONIC_SENSOR",
                                        "NFC_READER",
                                        "BLE_SCANNER",
                                        "CAMERA",
                                        "BARRIER_CONTROLLER",
                                        "DISPLAY_BOARD"
                                }
                        )
                );
    }
}
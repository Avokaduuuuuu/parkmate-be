package com.parkmate.mobileDevice;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.mobileDevice.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/user-service/mobile-device")
@RequiredArgsConstructor
@Tag(name = "Mobile Device Management", description = "APIs for managing mobile devices")
public class MobileDeviceController {

    private final MobileDeviceServiceImpl mobileDeviceService;

    @GetMapping()
    @Operation(
            summary = "Search Mobile Devices with Pagination",
            description = """
                    Retrieve a paginated list of mobile devices with optional filtering.
                    
                    **Search Criteria (MobileDeviceSearchRequest):**
                    - `deviceToken` (optional): Filter by device token
                    - `platform` (optional): Filter by platform (IOS, ANDROID)
                    - `userId` (optional): Filter by user ID
                    
                    **Returns:** Paginated list of mobile devices
                    """
    )
    public ResponseEntity<ApiResponse<Page<MobileDeviceResponse>>> getMobileDevices(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder,

            @ModelAttribute MobileDeviceSearchRequest request) {

        MobileDeviceSearchCriteria criteria = request.toCriteria();
        Page<MobileDeviceResponse> result = mobileDeviceService.searchDevices(
                criteria,
                page,
                size,
                sortBy,
                sortOrder);

        return ResponseEntity.ok(ApiResponse.<Page<MobileDeviceResponse>>builder()
                .success(true)
                .message("Mobile devices searched successfully")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build());
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Get Mobile Device by ID",
            description = """
                    Retrieve detailed information about a specific mobile device.
                    
                    **Parameters:**
                    - `id` (path): Mobile device UUID
                    
                    **Returns:** Mobile device details including token, platform, and user info
                    """
    )
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> getMobileDevice(@PathVariable Long id) {
        MobileDeviceResponse response = mobileDeviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device fetched successfully", response));
    }

    @PostMapping
    @Operation(
            summary = "Register Mobile Device",
            description = """
                    Register a new mobile device for push notifications.
                    
                    **Request Body (CreateMobileDeviceRequest):**
                    - `deviceToken` (required): FCM/APNS device token
                    - `platform` (required): Device platform (IOS, ANDROID)
                    - `userId` (required): Owner user ID
                    - `deviceName` (optional): Device name/model
                    - `osVersion` (optional): Operating system version
                    
                    **Returns:** Registered device information
                    """
    )
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> addMobileDevice(@RequestBody CreateMobileDeviceRequest req) {
        MobileDeviceResponse mobileDeviceResponse = mobileDeviceService.createMobileDevice(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mobile device created successfully", mobileDeviceResponse));
    }


    @Operation(
            summary = "Update Mobile Device",
            description = "Update the details of an existing mobile device."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> updateMobileDevice(
            @PathVariable Long id,
            @RequestBody UpdateMobileDeviceRequest req) {
        MobileDeviceResponse mobileDeviceResponse = mobileDeviceService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Mobile device updated successfully", mobileDeviceResponse));
    }

    @Operation(
            summary = "Delete Mobile Device",
            description = "Remove a mobile device from the system using its unique identifier."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMobileDevice(@PathVariable Long id) {
        mobileDeviceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device deleted successfully"));
    }
}
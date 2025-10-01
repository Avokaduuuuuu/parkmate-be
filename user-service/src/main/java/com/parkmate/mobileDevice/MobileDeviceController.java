package com.parkmate.mobileDevice;

import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.CreateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.MobileDeviceSearchRequest;
import com.parkmate.mobileDevice.dto.UpdateMobileDeviceRequest;
import com.parkmate.common.dto.ApiResponse;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mobile-device")
@RequiredArgsConstructor
@Tag(name = "Mobile Device Management", description = "APIs for managing mobile devices")
public class MobileDeviceController {

    private final MobileDeviceServiceImpl mobileDeviceService;

    @GetMapping()
    @Operation(
            summary = "Search Mobile Devices",
            description = "Retrieve a paginated list of mobile devices with optional filtering criteria."
    )
    public ResponseEntity<ApiResponse<Page<MobileDeviceResponse>>> getMobileDevices(
            @ModelAttribute MobileDeviceSearchRequest request,
            Pageable pageable) {

        MobileDeviceSearchCriteria criteria = request.toCriteria();
        Page<MobileDeviceResponse> result = mobileDeviceService.searchDevices(criteria, pageable);

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
            description = "Retrieve detailed information about a specific mobile device using its unique identifier."
    )
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> getMobileDevice(@PathVariable UUID id) {
        MobileDeviceResponse response = mobileDeviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device fetched successfully", response));
    }

    @Operation(
            summary = "Create Mobile Device",
            description = "Add a new mobile device to the system."
    )
    @PostMapping
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
            @PathVariable UUID id,
            @RequestBody UpdateMobileDeviceRequest req) {
        MobileDeviceResponse mobileDeviceResponse = mobileDeviceService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Mobile device updated successfully", mobileDeviceResponse));
    }

    @Operation(
            summary = "Delete Mobile Device",
            description = "Remove a mobile device from the system using its unique identifier."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMobileDevice(@PathVariable UUID id) {
        mobileDeviceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device deleted successfully"));
    }
}
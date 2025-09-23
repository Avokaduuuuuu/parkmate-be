package com.parkmate.controller;

import com.parkmate.dto.criteria.MobileDeviceSearchCriteria;
import com.parkmate.dto.request.CreateMobileDeviceRequest;
import com.parkmate.dto.request.MobileDeviceSearchRequest;
import com.parkmate.dto.request.UpdateMobileDeviceRequest;
import com.parkmate.dto.response.ApiResponse;
import com.parkmate.dto.response.MobileDeviceResponse;
import com.parkmate.service.impl.MobileDeviceServiceImpl;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-service/mobile-device")
@RequiredArgsConstructor
public class MobileDeviceController {

    private final MobileDeviceServiceImpl mobileDeviceService;

    @GetMapping()
    @Schema(description = "Search mobile devices with optional filters and pagination")
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
    @Schema(description = "Get mobile device details by ID")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> getMobileDevice(@PathVariable UUID id) {
        MobileDeviceResponse response = mobileDeviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device fetched successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> addMobileDevice(@RequestBody CreateMobileDeviceRequest req) {
        MobileDeviceResponse mobileDeviceResponse = mobileDeviceService.createMobileDevice(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mobile device created successfully", mobileDeviceResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> updateMobileDevice(
            @PathVariable UUID id,
            @RequestBody UpdateMobileDeviceRequest req) {
        MobileDeviceResponse mobileDeviceResponse = mobileDeviceService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Mobile device updated successfully", mobileDeviceResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMobileDevice(@PathVariable UUID id) {
        mobileDeviceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device deleted successfully", null));
    }
}
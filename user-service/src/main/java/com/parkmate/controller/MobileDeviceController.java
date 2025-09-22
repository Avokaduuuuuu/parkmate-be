package com.parkmate.controller;

import com.parkmate.dto.request.CreateMobileDeviceRequest;
import com.parkmate.dto.request.UpdateMobileDeviceRequest;
import com.parkmate.dto.response.ApiResponse;
import com.parkmate.dto.response.MobileDeviceResponse;
import com.parkmate.service.impl.MobileDeviceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-service/mobile-device")
@RequiredArgsConstructor
public class MobileDeviceController {

    private final MobileDeviceServiceImpl mobileDeviceService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MobileDeviceResponse>> getMobileDevice(@PathVariable UUID id) {
        MobileDeviceResponse response = mobileDeviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Mobile device fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MobileDeviceResponse>>> getMobileDevices(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Page<MobileDeviceResponse> mobileDeviceResponse = mobileDeviceService.findAll(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Mobile devices fetched successfully", mobileDeviceResponse));
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
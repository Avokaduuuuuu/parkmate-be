package com.parkmate.service;

import com.parkmate.dto.request.CreateMobileDeviceRequest;
import com.parkmate.dto.request.UpdateMobileDeviceRequest;
import com.parkmate.dto.response.MobileDeviceResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MobileDeviceService {

    MobileDeviceResponse createMobileDevice(CreateMobileDeviceRequest request);

    MobileDeviceResponse update(UUID id, UpdateMobileDeviceRequest request);

    MobileDeviceResponse findById(UUID id);

    Page<MobileDeviceResponse> findAll(int page, int size, String sort);

    void delete(UUID id);

}

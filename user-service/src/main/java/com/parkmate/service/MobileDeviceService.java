package com.parkmate.service;

import com.parkmate.dto.criteria.MobileDeviceSearchCriteria;
import com.parkmate.dto.request.CreateMobileDeviceRequest;
import com.parkmate.dto.request.UpdateMobileDeviceRequest;
import com.parkmate.dto.response.MobileDeviceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MobileDeviceService {

    Page<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria, Pageable pageable);

    List<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria);

    MobileDeviceResponse createMobileDevice(CreateMobileDeviceRequest request);

    MobileDeviceResponse update(UUID id, UpdateMobileDeviceRequest request);

    MobileDeviceResponse findById(UUID id);

    Page<MobileDeviceResponse> findAll(int page, int size, String sort);

    void delete(UUID id);

}

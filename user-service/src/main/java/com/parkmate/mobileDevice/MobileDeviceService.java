package com.parkmate.mobileDevice;

import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.CreateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.UpdateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
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

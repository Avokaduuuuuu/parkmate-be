package com.parkmate.mobileDevice;

import com.parkmate.mobileDevice.dto.CreateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.UpdateMobileDeviceRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MobileDeviceService {

    Page<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria, int page, int size, String sortBy, String sortOrder);

    List<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria);

    MobileDeviceResponse createMobileDevice(CreateMobileDeviceRequest request);

    MobileDeviceResponse update(Long id, UpdateMobileDeviceRequest request);

    MobileDeviceResponse findById(Long id);

    Page<MobileDeviceResponse> findAll(int page, int size, String sort);

    void delete(Long id);

}

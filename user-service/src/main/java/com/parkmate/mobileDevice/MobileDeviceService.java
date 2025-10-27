package com.parkmate.mobileDevice;

import com.parkmate.mobileDevice.dto.CreateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.UpdateMobileDeviceRequest;
import org.springframework.data.domain.Page;

public interface MobileDeviceService {

    Page<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria, int page, int size, String sortBy, String sortOrder);

    MobileDeviceResponse createMobileDevice(CreateMobileDeviceRequest request, String userHeadId);

    MobileDeviceResponse update(Long id, UpdateMobileDeviceRequest request);

    MobileDeviceResponse findById(Long id);

    void delete(Long id);

}

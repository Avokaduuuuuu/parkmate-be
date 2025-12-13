package com.parkmate.device;

import com.parkmate.device.dto.req.DeviceCreateRequest;
import com.parkmate.device.dto.req.DeviceUpdateRequest;
import com.parkmate.device.dto.resp.DeviceResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DeviceService {
    Page<DeviceResponse> getDevices(
            int page,
            int size,
            String sortBy,
            String sortOrder,
            DeviceFilterParams filterParams
    );

    DeviceResponse getDeviceById(Long id);

    List<DeviceResponse> createDevices(Long lotId, List<DeviceCreateRequest> requests);
    DeviceResponse updateDevice(Long id, DeviceUpdateRequest request);
    void deleteDevice(Long id);
    Long countDevices();
}

package com.parkmate.device_fee_config;

import com.parkmate.device_fee_config.dto.req.DeviceFeeConfigCreateRequest;
import com.parkmate.device_fee_config.dto.req.DeviceFeeConfigUpdateRequest;
import com.parkmate.device_fee_config.dto.resp.DeviceFeeConfigResponse;
import org.springframework.data.domain.Page;

public interface DeviceFeeConfigService {
    Page<DeviceFeeConfigResponse> fetchDeviceFeeConfigs(int page,
                                                        int size,
                                                        String sortBy,
                                                        String sortOrder,
                                                        DeviceFeeConfigFilterParams params);
    DeviceFeeConfigResponse fetchyById(Long id);
    DeviceFeeConfigResponse addDeviceFeeConfig(DeviceFeeConfigCreateRequest request);
    DeviceFeeConfigResponse updateDeviceFeeConfig(DeviceFeeConfigUpdateRequest request, Long id);
    void deleteDeviceFeeConfig(Long id);
}

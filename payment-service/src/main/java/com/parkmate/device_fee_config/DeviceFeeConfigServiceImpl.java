package com.parkmate.device_fee_config;

import com.parkmate.device_fee_config.dto.req.DeviceFeeConfigCreateRequest;
import com.parkmate.device_fee_config.dto.req.DeviceFeeConfigUpdateRequest;
import com.parkmate.device_fee_config.dto.resp.DeviceFeeConfigResponse;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceFeeConfigServiceImpl implements DeviceFeeConfigService {
    private final DeviceFeeConfigRepository deviceFeeConfigRepository;
    @Override
    public Page<DeviceFeeConfigResponse> fetchDeviceFeeConfigs(int page, int size, String sortBy, String sortOrder, DeviceFeeConfigFilterParams params) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DeviceFeeConfigEntity> deviceFeeConfigEntityPage = deviceFeeConfigRepository.findAll(params.getSpecification(),pageable);
        return deviceFeeConfigEntityPage.map(DeviceFeeConfigMapper.INSTANCE::toResponse);
    }

    @Override
    public DeviceFeeConfigResponse fetchyById(Long id) {
        return DeviceFeeConfigMapper.INSTANCE.toResponse(deviceFeeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_FEE_CONFIG_NOT_FOUND, "Device fee config with id " + id +  " not found")));
    }

    @Override
    public DeviceFeeConfigResponse addDeviceFeeConfig(DeviceFeeConfigCreateRequest request) {
        DeviceFeeConfigEntity deviceFeeConfigEntity = new DeviceFeeConfigEntity();
        BeanUtils.copyProperties(request, deviceFeeConfigEntity);
        deviceFeeConfigEntity.setIsActive(false);
        return DeviceFeeConfigMapper.INSTANCE.toResponse(deviceFeeConfigRepository.save(deviceFeeConfigEntity));
    }

    @Override
    public DeviceFeeConfigResponse updateDeviceFeeConfig(DeviceFeeConfigUpdateRequest request, Long id) {
        DeviceFeeConfigEntity deviceFeeConfigEntity = deviceFeeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_FEE_CONFIG_NOT_FOUND, "Device fee config with id " + id + " not found"));

        if (request.deviceType() != null) {
            deviceFeeConfigEntity.setDeviceType(request.deviceType());
        }
        if (request.deviceFee() != null) {
            deviceFeeConfigEntity.setDeviceFee(request.deviceFee());
        }
        if (request.validFrom() != null) {
            deviceFeeConfigEntity.setValidFrom(request.validFrom());
        }
        if (request.validUntil() != null) {
            deviceFeeConfigEntity.setValidUntil(request.validUntil());
        }
        if (request.isActive() != null) {
            if (request.isActive()) {
                deviceFeeConfigRepository.deactivateDeviceFeeConfig(deviceFeeConfigEntity.getDeviceType());
                deviceFeeConfigEntity.setIsActive(true);
            } else deviceFeeConfigEntity.setIsActive(false);
        }
        return DeviceFeeConfigMapper.INSTANCE.toResponse(deviceFeeConfigRepository.save(deviceFeeConfigEntity));
    }

    @Override
    public void deleteDeviceFeeConfig(Long id) {
        DeviceFeeConfigEntity deviceFeeConfigEntity = deviceFeeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_FEE_CONFIG_NOT_FOUND, "Device fee config with id " + id + " not found"));

        deviceFeeConfigRepository.delete(deviceFeeConfigEntity);
    }
}

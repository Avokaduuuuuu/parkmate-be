package com.parkmate.device_fee_config;

import com.parkmate.common.enums.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceFeeConfigRepository extends JpaRepository<DeviceFeeConfigEntity, Long> {
    Optional<DeviceFeeConfigEntity> findByDeviceType(DeviceType deviceType);
}

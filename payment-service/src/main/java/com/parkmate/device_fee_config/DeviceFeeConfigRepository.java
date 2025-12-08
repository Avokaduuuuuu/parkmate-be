package com.parkmate.device_fee_config;

import com.parkmate.common.enums.DeviceType;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DeviceFeeConfigRepository extends JpaRepository<DeviceFeeConfigEntity, Long>, JpaSpecificationExecutor<DeviceFeeConfigEntity> {
    Optional<DeviceFeeConfigEntity> findByDeviceType(DeviceType deviceType);

    @Modifying
    @Query(value = """
    UPDATE device_fee_config
    SET is_active = CASE 
        WHEN id = :id THEN true 
        ELSE false 
    END
""", nativeQuery = true)
    void activateOnlyNative(@Param("id") Long id);
}

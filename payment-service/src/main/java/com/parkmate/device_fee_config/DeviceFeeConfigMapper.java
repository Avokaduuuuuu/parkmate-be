package com.parkmate.device_fee_config;

import com.parkmate.device_fee_config.dto.resp.DeviceFeeConfigResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DeviceFeeConfigMapper {
    DeviceFeeConfigMapper INSTANCE = Mappers.getMapper(DeviceFeeConfigMapper.class);
    DeviceFeeConfigResponse toResponse(DeviceFeeConfigEntity entity);
}

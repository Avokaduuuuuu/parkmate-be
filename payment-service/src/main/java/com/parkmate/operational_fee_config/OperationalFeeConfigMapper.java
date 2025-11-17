package com.parkmate.operational_fee_config;

import com.parkmate.operational_fee_config.dto.resp.OperationalFeeConfigResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OperationalFeeConfigMapper {
    OperationalFeeConfigMapper INSTANCE = Mappers.getMapper(OperationalFeeConfigMapper.class);
    OperationalFeeConfigResponse toResponse(OperationalFeeConfigEntity entity);
}

package com.parkmate.operationalFeeConfig;

import com.parkmate.operationalFeeConfig.dto.resp.OperationalFeeConfigResponse;
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

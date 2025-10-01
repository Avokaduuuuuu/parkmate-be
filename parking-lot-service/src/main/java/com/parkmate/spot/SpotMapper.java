package com.parkmate.spot;

import com.parkmate.spot.dto.req.SpotCreateRequest;
import com.parkmate.spot.dto.resp.SpotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface SpotMapper {
    SpotMapper INSTANCE = Mappers.getMapper(SpotMapper.class);

    SpotEntity toEntity(SpotCreateRequest spotCreateRequest);
    SpotResponse toResponse(SpotEntity spotEntity);
}

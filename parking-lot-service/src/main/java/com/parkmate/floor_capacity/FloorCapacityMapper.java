package com.parkmate.floor_capacity;

import com.parkmate.floor_capacity.dto.resp.FloorCapacityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface FloorCapacityMapper {
    FloorCapacityMapper INSTANCE = Mappers.getMapper(FloorCapacityMapper.class);

    FloorCapacityResponse toResponse(FloorCapacityEntity entity);
}

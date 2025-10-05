package com.parkmate.floor;

import com.parkmate.area.AreaMapper;
import com.parkmate.floor.dto.resp.FloorDetailedResponse;
import com.parkmate.floor.dto.resp.FloorResponse;
import com.parkmate.floor_capacity.FloorCapacityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {FloorCapacityMapper.class, AreaMapper.class}
)
public interface FloorMapper {
    FloorMapper INSTANCE = Mappers.getMapper(FloorMapper.class);

    FloorResponse toResponse(FloorEntity entity);
    FloorDetailedResponse toResponseDetailed(FloorEntity entity);
}

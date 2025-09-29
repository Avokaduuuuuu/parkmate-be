package com.parkmate.area;

import com.parkmate.area.dto.resp.AreaResponse;
import com.parkmate.spot.SpotMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        uses = {SpotMapper.class}
)
public interface AreaMapper {
    AreaMapper INSTANCE = Mappers.getMapper(AreaMapper.class);

    AreaResponse toResponse(AreaEntity entity);
}

package com.parkmate.mapper;

import com.parkmate.dto.resp.ParkingFloorResponse;
import com.parkmate.entity.ParkingFloorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ParkingFloorCapacityMapper.class}
)
public interface ParkingFloorMapper {
    ParkingFloorMapper INSTANCE = Mappers.getMapper(ParkingFloorMapper.class);

    ParkingFloorResponse toResponse(ParkingFloorEntity entity);
}

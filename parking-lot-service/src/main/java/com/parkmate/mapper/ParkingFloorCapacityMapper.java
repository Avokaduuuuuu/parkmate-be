package com.parkmate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface ParkingFloorCapacityMapper {
    ParkingFloorCapacityMapper INSTANCE = Mappers.getMapper(ParkingFloorCapacityMapper.class);


}

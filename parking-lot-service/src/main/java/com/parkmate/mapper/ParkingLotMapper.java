package com.parkmate.mapper;

import com.parkmate.dto.req.ParkingLotCreateRequest;
import com.parkmate.dto.resp.ParkingLotResponse;
import com.parkmate.entity.ParkingLotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface ParkingLotMapper {
    ParkingLotMapper INSTANCE = Mappers.getMapper(ParkingLotMapper.class);

    @Mapping(target = "openTime", source = "operatingHoursStart")
    @Mapping(target = "closeTime", source = "operatingHoursEnd")
    ParkingLotResponse toResponse(ParkingLotEntity entity);

    ParkingLotEntity toEntity(ParkingLotCreateRequest request);
}

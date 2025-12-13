package com.parkmate.device;

import com.parkmate.device.dto.resp.DeviceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DeviceMapper {
    DeviceMapper INSTANCE = Mappers.getMapper(DeviceMapper.class);

    @Mapping(target = "lotId", source = "parkingLot.id")
    @Mapping(target = "lotName", source = "parkingLot.name")
    DeviceResponse toResponse(DeviceEntity entity);
}

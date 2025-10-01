package com.parkmate.lot_capacity;


import com.parkmate.lot_capacity.dto.resp.LotCapacityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface LotCapacityMapper {

    LotCapacityMapper INSTANCE = Mappers.getMapper(LotCapacityMapper.class);

    LotCapacityResponse toResponse(LotCapacityEntity entity);
}

package com.parkmate.device_payment_item;

import com.parkmate.device_payment_item.dto.resp.DevicePaymentItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DevicePaymentItemMapper {
    DevicePaymentItemMapper INSTANCE = Mappers.getMapper(DevicePaymentItemMapper.class);

    DevicePaymentItemResponse toResponse(DevicePaymentItemEntity entity);
}

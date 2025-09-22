package com.parkmate.mapper;

import com.parkmate.config.MapStructConfig;
import com.parkmate.dto.request.CreateMobileDeviceRequest;
import com.parkmate.dto.request.UpdateMobileDeviceRequest;
import com.parkmate.dto.response.MobileDeviceResponse;
import com.parkmate.entity.MobileDevice;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface MobileDeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Will be set manually in service
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "lastActiveAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    MobileDevice toEntity(CreateMobileDeviceRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "user.fullName", source = "user.fullName")
    MobileDeviceResponse toDTO(MobileDevice mobileDevice);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deviceId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastActiveAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateMobileDeviceRequest request, @MappingTarget MobileDevice existingMobileDevice);

}

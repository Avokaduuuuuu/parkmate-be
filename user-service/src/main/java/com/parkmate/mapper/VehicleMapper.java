package com.parkmate.mapper;

import com.parkmate.config.MapStructConfig;
import com.parkmate.dto.request.CreateVehicleRequest;
import com.parkmate.dto.request.UpdateVehicleRequest;
import com.parkmate.dto.response.VehicleResponse;
import com.parkmate.entity.Vehicle;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface VehicleMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "licenseImage", ignore = true),
            @Mapping(target = "isActive", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),

    })
    Vehicle toEntity(CreateVehicleRequest vehicleDTO);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isElectric", source = "electric")
    VehicleResponse toDTO(Vehicle vehicle);

    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "licensePlate", ignore = true),
            @Mapping(target = "active", ignore = true), //test
            @Mapping(target = "electric", source = "isElectric")
    })
    void updateEntityFromDTO(UpdateVehicleRequest vehicleDTO, @MappingTarget Vehicle vehicle);

}

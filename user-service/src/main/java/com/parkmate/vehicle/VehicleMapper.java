package com.parkmate.vehicle;

import com.parkmate.common.config.MapStructConfig;
import com.parkmate.s3.S3Service;
import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapStructConfig.class)
public abstract class VehicleMapper {

    @Autowired
    protected S3Service s3Service;

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "licenseImage", ignore = true),
            @Mapping(target = "isActive", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "isElectric", source = "electric")

    })
    public abstract Vehicle toEntity(CreateVehicleRequest vehicleDTO);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isElectric", source = "electric")
    @Mapping(target = "vehiclePhotoUrl", source = "licenseImage", qualifiedByName = "generatePresignedUrl")
    public abstract VehicleResponse toDTO(Vehicle vehicle);

    @Named("generatePresignedUrl")
    protected String generatePresignedUrl(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return null;
        }
        return s3Service.generatePresignedUrl(s3Key);
    }

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
    public abstract void updateEntityFromDTO(UpdateVehicleRequest vehicleDTO, @MappingTarget Vehicle vehicle);

}

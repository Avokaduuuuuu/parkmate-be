package com.parkmate.user;

import com.parkmate.common.config.MapStructConfig;
import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "profilePictureUrl", ignore = true),
            @Mapping(target = "frontPhotoPath", ignore = true),
            @Mapping(target = "backPhotoPath", ignore = true)
    })
    void updateEntity(UpdateUserRequest req, @MappingTarget User user);

    @Mappings({
            @Mapping(target = "frontPhotoPresignedUrl", ignore = true),
            @Mapping(target = "backPhotoPresignedUrl", ignore = true),
            @Mapping(target = "profilePicturePresignedUrl", ignore = true),
            @Mapping(target = "account", ignore = true)
    })
    UserResponse toResponse(User user);
}

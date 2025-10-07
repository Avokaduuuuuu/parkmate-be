package com.parkmate.user;

import com.parkmate.common.config.MapStructConfig;
import com.parkmate.user.dto.CreateUserRequest;
import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            // tạm thời ignore các trường không có trong request
            @Mapping(target = "dateOfBirth", ignore = true),
            @Mapping(target = "address", ignore = true),
            @Mapping(target = "profilePictureUrl", ignore = true),
            @Mapping(target = "idNumber", ignore = true),
            @Mapping(target = "issuePlace", ignore = true),
            @Mapping(target = "issueDate", ignore = true),
            @Mapping(target = "expiryDate", ignore = true),
            @Mapping(target = "frontPhotoPath", ignore = true),
            @Mapping(target = "backPhotoPath", ignore = true),
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "fullName", ignore = true),
            @Mapping(target = "account", ignore = true) // test

    })
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "fullName", ignore = true),
// test
    })
    void updateEntity(UpdateUserRequest req, @MappingTarget User user);

    UserResponse toResponse(User user);
}

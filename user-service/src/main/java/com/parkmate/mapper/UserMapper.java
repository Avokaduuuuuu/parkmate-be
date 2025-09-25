package com.parkmate.mapper;

import com.parkmate.config.MapStructConfig;
import com.parkmate.dto.request.CreateUserRequest;
import com.parkmate.dto.request.UpdateUserRequest;
import com.parkmate.dto.response.UserResponse;
import com.parkmate.entity.User;
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
            @Mapping(target = "account", ignore = true) // test

    })
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "account", ignore = true) // test
    })
    void updateEntity(UpdateUserRequest req, @MappingTarget User user);

    UserResponse toResponse(User user);
}

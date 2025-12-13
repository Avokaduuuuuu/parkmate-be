package com.parkmate.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.common.config.MapStructConfig;
import com.parkmate.common.util.QRCodeGenerator;
import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

@Mapper(config = MapStructConfig.class)
@Slf4j
public abstract class UserMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "vehicles", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "profilePictureUrl", ignore = true),
            @Mapping(target = "frontPhotoPath", ignore = true),
            @Mapping(target = "backPhotoPath", ignore = true),
            @Mapping(target = "reservations", ignore = true),
            @Mapping(target = "subscriptions", ignore = true)
    })
    public abstract void updateEntity(UpdateUserRequest req, @MappingTarget User user);

    @Mappings({
            @Mapping(target = "frontPhotoPresignedUrl", ignore = true),
            @Mapping(target = "backPhotoPresignedUrl", ignore = true),
            @Mapping(target = "profilePicturePresignedUrl", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "qrCode", expression = "java(generateUserQRCode(user))")
    })
    public abstract UserResponse toResponse(User user);

    protected String generateUserQRCode(User user) {
        try {
            Map<String, Object> qrData = new LinkedHashMap<>();
            qrData.put("userId", user.getId());
            qrData.put("qrType", "memberWalkIn");
            String jsonContent = objectMapper.writeValueAsString(qrData);
            return QRCodeGenerator.generateQRCodeBase64(jsonContent);
        } catch (Exception e) {
            log.error("Error generating QR code for user ID: {}", user.getId(), e);
            try {
                String fallbackContent = String.format("{\"userId\":%d}", user.getId());
                return QRCodeGenerator.generateQRCodeBase64(fallbackContent);
            } catch (Exception ex) {
                log.error("Error generating fallback QR code for user ID: {}", user.getId(), ex);
                return null;
            }
        }
    }

}

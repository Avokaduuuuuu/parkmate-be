package com.parkmate.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.common.config.MapStructConfig;
import com.parkmate.common.util.QRCodeGenerator;
import com.parkmate.user.dto.UpdateUserRequest;
import com.parkmate.user.dto.UserResponse;
import com.parkmate.vehicle.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Generate QR code for user containing userId and vehicle list
     * Format: {"userId": 123, "vehicles": [{"plate": "30A-12345", "type": "CAR_UP_TO_9_SEATS", "electric": false}]}
     *
     * @param user User entity
     * @return Base64 encoded QR code image with data URI prefix
     */
    protected String generateUserQRCode(User user) {
        try {
            Map<String, Object> qrData = new LinkedHashMap<>();
            qrData.put("userId", user.getId());
            qrData.put("qrType", "memberWalkIn");
            if (user.getVehicles() != null && !user.getVehicles().isEmpty()) {
                List<Map<String, Object>> vehicleList = user.getVehicles().stream()
                        .filter(Vehicle::isActive) // Only include active vehicles
                        .map(vehicle -> {
                            Map<String, Object> vehicleInfo = new LinkedHashMap<>();
                            vehicleInfo.put("plate", vehicle.getLicensePlate());
                            vehicleInfo.put("type", vehicle.getVehicleType().name());
                            vehicleInfo.put("electric", vehicle.isElectric());
                            return vehicleInfo;
                        })
                        .collect(Collectors.toList());

                qrData.put("vehicles", vehicleList);
            }

            String jsonContent = objectMapper.writeValueAsString(qrData);

            log.debug("Generated QR code content for user ID: {} with {} vehicles",
                    user.getId(),
                    user.getVehicles() != null ? user.getVehicles().size() : 0);

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

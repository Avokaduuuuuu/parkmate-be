package com.parkmate.device.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.device.enums.DeviceStatus;
import com.parkmate.device.enums.DeviceType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceResponse {
    Long id;
    String deviceId;
    String deviceName;
    DeviceType deviceType;
    Long partnerId;
    String model;
    String serialNumber;
    DeviceStatus status;
    Boolean isActive;
    LocalDateTime lastMaintainedAt;
    String notes;
    Long lotId;
    String lotName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}


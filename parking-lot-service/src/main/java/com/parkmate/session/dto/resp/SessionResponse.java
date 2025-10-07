package com.parkmate.session.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.session.enums.AuthMethod;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.session.enums.SessionType;
import com.parkmate.session.enums.SyncStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionResponse {
    UUID id;
    Long userId;
    Long vehicleId;
    String licensePlate;
    SessionType sessionType;
    AuthMethod authMethod;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime entryTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime exitTime;
    Integer durationMinute;
    BigDecimal totalAmount;
    SessionStatus sessionStatus;
    SyncStatus syncStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime syncedTime;
    String note;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    Long lotId;
    Long spotId;
    String cardUUID;
    Long pricingRuleId;

}

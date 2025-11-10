package com.parkmate.session.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import com.parkmate.session.enums.ReferenceType;
import com.parkmate.session.enums.SessionStatus;
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
public class SessionDetailedResponse {
    UUID id;
    Long userId;
    String licensePlate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime entryTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime exitTime;
    Integer durationMinute;
    BigDecimal totalAmount;
    SessionStatus status;
    SyncStatus syncStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime syncedFromLocal;
    String note;
    String entryImage;
    String entryPlateImage;
    String exitImage;
    String exitPlateImage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    String lotName;
    String cardUUID;
    PricingRuleResponse pricingRule;
    Long referenceId;
    ReferenceType referenceType;
}

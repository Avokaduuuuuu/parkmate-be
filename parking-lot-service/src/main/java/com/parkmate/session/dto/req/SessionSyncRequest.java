package com.parkmate.session.dto.req;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.AuthMethod;
import com.parkmate.session.enums.ReferenceType;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.session.enums.SessionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionSyncRequest(
        String id,
        Long lotId,
        String licensePlate,
        VehicleType vehicleType,
        AuthMethod authMethod,
        SessionType sessionType,
        Long userId,
        ReferenceType referenceType,
        Long referenceId,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        String entryImage,
        String entryImagePlate,
        String exitImage,
        String exitImagePlate,
        Integer durationMinute,
        BigDecimal totalAmount,
        SessionStatus status,
        String cardUUID,
        Long pricingRuleId,
        String note
) {
}

package com.parkmate.session.dto.req;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionSyncRequest(
        UUID id,
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
        Integer durationMinute,
        BigDecimal totalAmount,
        SessionStatus status,
        String cardUUID,
        Long pricingRuleId,
        String note
) {
}

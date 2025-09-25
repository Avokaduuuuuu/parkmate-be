package com.parkmate.mobileDevice;


import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.entity.QMobileDevice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
public class MobileDeviceSpecification {

    public static Predicate buildPredicate(MobileDeviceSearchCriteria criteria) {
        QMobileDevice mobileDevice = QMobileDevice.mobileDevice;
        BooleanBuilder builder = new BooleanBuilder();

        // User filters
        if (criteria.getUserId() != null) {
            builder.and(mobileDevice.user.id.eq(criteria.getUserId()));
        }

        if (criteria.getUserIds() != null && !criteria.getUserIds().isEmpty()) {
            builder.and(mobileDevice.user.id.in(criteria.getUserIds()));
        }

        // Device ID
        if (StringUtils.hasText(criteria.getDeviceId())) {
            builder.and(mobileDevice.deviceId.eq(criteria.getDeviceId()));
        }

        // Device name search
        if (StringUtils.hasText(criteria.getDeviceName())) {
            builder.and(mobileDevice.deviceName.containsIgnoreCase(criteria.getDeviceName()));
        }

        // Single OS
        if (criteria.getDeviceOs() != null) {
            builder.and(mobileDevice.deviceOs.eq(criteria.getDeviceOs()));
        }

        // Multiple OS
        if (criteria.getDeviceOsList() != null && !criteria.getDeviceOsList().isEmpty()) {
            builder.and(mobileDevice.deviceOs.in(criteria.getDeviceOsList()));
        }

        // Active filter
        if (criteria.getIsActive() != null) {
            builder.and(mobileDevice.isActive.eq(criteria.getIsActive()));
        }

        // Last active filters
        if (criteria.getLastActiveAfter() != null) {
            builder.and(mobileDevice.lastActiveAt.after(criteria.getLastActiveAfter()));
        }

        if (criteria.getLastActiveBefore() != null) {
            builder.and(mobileDevice.lastActiveAt.before(criteria.getLastActiveBefore()));
        }

        // Inactive days
        if (criteria.getInactiveDays() != null) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(criteria.getInactiveDays());
            builder.and(mobileDevice.lastActiveAt.before(cutoff)
                    .or(mobileDevice.lastActiveAt.isNull()));
        }

        // Date range
        if (criteria.getCreatedAfter() != null) {
            builder.and(mobileDevice.createdAt.after(criteria.getCreatedAfter()));
        }

        if (criteria.getCreatedBefore() != null) {
            builder.and(mobileDevice.createdAt.before(criteria.getCreatedBefore()));
        }

        return builder;
    }
}
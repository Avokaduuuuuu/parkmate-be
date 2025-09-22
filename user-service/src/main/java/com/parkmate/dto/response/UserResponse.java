package com.parkmate.dto.response;

import com.parkmate.entity.enums.UserRole;
import com.parkmate.entity.enums.UserStatus;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String phone,
        String firstName,
        String lastName,
        UserStatus status,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}

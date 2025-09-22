package com.parkmate.dto.request;

import com.parkmate.entity.enums.UserRole;
import com.parkmate.entity.enums.UserStatus;

import java.time.LocalDate;

public record UpdateUserRequest(
        String phone,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        String profilePictureUrl,
        String idNumber,
        String issuePlace,
        LocalDate issueDate,
        LocalDate expiryDate,
        String frontPhotoPath,
        String backPhotoPath,
        UserStatus status,
        UserRole role
) {
}


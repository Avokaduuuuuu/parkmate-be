package com.parkmate.user.dto;

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
        String backPhotoPath
) {
}


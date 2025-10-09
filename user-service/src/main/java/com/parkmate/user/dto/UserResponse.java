package com.parkmate.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.account.dto.AccountBasicResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(

        AccountBasicResponse account,
        Long id,
        String phone,
        String firstName,
        String lastName,
        String fullName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,
        String address,
        String gender,
        String nationality,
        String profilePictureUrl,
        String idNumber,
        String issuePlace,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate issueDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate expiryDate,
        String frontPhotoPresignedUrl,
        String backPhotoPresignedUrl,
        String profilePicturePresignedUrl,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}

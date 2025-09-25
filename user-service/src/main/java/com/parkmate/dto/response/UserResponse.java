package com.parkmate.dto.response;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;



public record UserResponse(
        Long id,
        String phone,
        String firstName,
        String lastName,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdAt,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime updatedAt
) {
}

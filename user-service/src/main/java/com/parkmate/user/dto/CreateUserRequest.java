package com.parkmate.user.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank String phone,
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String address
) {
}


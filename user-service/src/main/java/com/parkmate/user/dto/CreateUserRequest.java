package com.parkmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Create user request")
public record CreateUserRequest(
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must be 10-12 digits")
        @Schema(description = "Phone number (10-12 digits, numbers only)", example = "0123456789", requiredMode = Schema.RequiredMode.REQUIRED)
        String phone,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "User's first name (max 100 chars)", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        @Schema(description = "User's last name (max 50 chars)", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @Past(message = "Date of birth must be in the past")
        @Schema(description = "Date of birth (optional, must be in the past)", example = "1990-01-15")
        LocalDate dateOfBirth,

        @Size(max = 100, message = "Address must not exceed 100 characters")
        @Schema(description = "Residential address (optional, max 100 chars)", example = "123 Main Street, District 1, HCMC")
        String address
) {
}


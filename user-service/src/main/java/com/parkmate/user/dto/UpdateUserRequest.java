package com.parkmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Update user request - all fields are optional")
public record UpdateUserRequest(
        @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must be 10-12 digits")
        @Schema(description = "Phone number (10-12 digits, numbers only)", example = "0123456789")
        String phone,

        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "User's first name (max 100 chars)", example = "John")
        String firstName,

        @Size(max = 50, message = "Last name must not exceed 50 characters")
        @Schema(description = "User's last name (max 50 chars)", example = "Doe")
        String lastName,

        @Past(message = "Date of birth must be in the past")
        @Schema(description = "Date of birth (must be in the past)", example = "1990-01-15")
        LocalDate dateOfBirth,

        @Size(max = 100, message = "Address must not exceed 100 characters")
        @Schema(description = "Residential address (max 100 chars)", example = "123 Main Street, District 1, HCMC")
        String address,

        @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
        @Schema(description = "Profile picture URL (max 500 chars)", example = "/uploads/profile/user123.jpg")
        String profilePictureUrl,

        @Pattern(regexp = "^[0-9]{9,12}$", message = "ID number must be 9-12 digits")
        @Schema(description = "National ID number (9-12 digits)", example = "079012345678")
        String idNumber,

        @Size(max = 100, message = "Issue place must not exceed 100 characters")
        @Schema(description = "ID card issue place (max 100 chars)", example = "Public Security Department of HCMC")
        String issuePlace,

        @Schema(description = "ID card issue date", example = "2020-01-15")
        LocalDate issueDate,

        @Schema(description = "ID card expiry date", example = "2030-01-15")
        LocalDate expiryDate,

        @Size(max = 500, message = "Photo path must not exceed 500 characters")
        @Schema(description = "Front ID card photo path/URL (max 500 chars)", example = "/uploads/id/front_123.jpg")
        String frontPhotoPath,

        @Size(max = 500, message = "Photo path must not exceed 500 characters")
        @Schema(description = "Back ID card photo path/URL (max 500 chars)", example = "/uploads/id/back_123.jpg")
        String backPhotoPath
) {
}


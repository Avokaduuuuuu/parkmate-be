package com.parkmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Customer (user) registration request")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores and hyphens")
    @Schema(description = "Unique username", example = "john_doe")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(description = "User password (min 8 characters)", example = "SecurePass123!")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must be 10-12 digits")
    @Schema(description = "Phone number (10-12 digits)", example = "0123456789")
    private String phone;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^[0-9]{9,12}$", message = "ID number must be 9-12 digits")
    @Schema(description = "National ID number (9-12 digits)", example = "079012345678")
    private String idNumber;

    @Schema(description = "Date of birth", example = "1990-01-15T00:00:00")
    private LocalDateTime dateOfBirth;

    @Size(max = 100, message = "Address must not exceed 100 characters")
    @Schema(description = "Residential address", example = "123 Main Street, District 1, HCMC")
    private String address;

    @Size(max = 500, message = "Photo path must not exceed 500 characters")
    @Schema(description = "Front ID card photo path/URL", example = "/uploads/id/front_123.jpg")
    private String frontPhotoPath;

    @Size(max = 500, message = "Photo path must not exceed 500 characters")
    @Schema(description = "Back ID card photo path/URL", example = "/uploads/id/back_123.jpg")
    private String backPhotoPath;

}

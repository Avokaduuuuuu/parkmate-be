package com.parkmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
    @Schema(description = "[MUST BE UNIQUE] User email address ",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(description = "User password (minimum 8 characters, max 100)", example = "12341234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must be 10-12 digits")
    @Schema(description = "[MUST BE UNIQUE] Phone number (10-12 digits, numbers only)", example = "0123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "User's first name (, max 100 chars)", example = "Trịnh")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "User's last name (, max 50 chars)", example = "Trần Phương Tuấn")
    private String lastName;

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    @Schema(description = "User's full name (, max 150 chars)", example = "Trịnh Trần Phương Tuấn")
    private String fullName;

    @Pattern(regexp = "^[0-9]{9,12}$", message = "ID number must be 9-12 digits")
    @Schema(description = "[MUST BE UNIQUE] National ID number (, 9-12 digits if provided)", example = "079012345678")
    private String idNumber;

    @Schema(description = "Date of birth (, must be in the past if provided)", example = "1990-01-15T00:00:00")
    @Past(message = "Date of birth must be in the past")
    private LocalDateTime dateOfBirth;

    @Schema(description = "ID card issue place", example = "Public Security Department of HCMC")
    private String issuePlace;

    @Schema(description = "ID card issue date", example = "2020-01-15T00:00:00")
    private LocalDateTime issueDate;

    @Schema(description = "ID card expiry date", example = "2030-01-15T00:00:00")
    private LocalDateTime expiryDate;

    @Size(max = 100, message = "Address must not exceed 100 characters")
    @Schema(description = "Residential address (, max 100 chars)", example = "123 Main Street, District 1, HCMC")
    private String address;

    @Schema(
            description = "s3 key of front side of ID card image",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String frontIdPath;

    @Schema(
            description = "s3 key of back side of ID card image",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String backIdImgPath;

}

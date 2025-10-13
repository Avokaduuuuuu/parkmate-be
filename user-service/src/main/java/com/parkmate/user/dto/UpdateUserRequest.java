package com.parkmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Update user request - all fields are optional")
@Builder
@Data
public class UpdateUserRequest {

    boolean ownedByMe;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must be 10-12 digits")
    @Schema(description = "Phone number (10-12 digits, numbers only)", example = "0123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    String phone;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "User's first name (max 100 chars)", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "User's last name (max 50 chars)", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    String lastName;

    @Size(max = 100, message = "Address must not exceed 100 characters")
    @Schema(description = "Residential address (optional, max 100 chars)", example = "123 Main Street, District 1, HCMC")
    String address;

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    @Schema(description = "User's full name (, max 150 chars)", example = "Trịnh Trần Phương Tuấn")
    private String fullName;

    @Pattern(regexp = "^(Male|Female|Other)?$")
    @Schema(description = "")
    private String gender;

    @Schema(description = "")
    private String nationality;

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


}


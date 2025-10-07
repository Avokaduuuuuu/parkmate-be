package com.parkmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

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

}

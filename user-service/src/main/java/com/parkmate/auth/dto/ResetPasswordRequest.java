package com.parkmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Reset password request")
public record ResetPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "User's email address", example = "user@example.com")
        String email,

        @NotBlank(message = "Reset code is required")
        @Size(min = 6, max = 6, message = "Reset code must be 6 digits")
        @Schema(description = "6-digit reset code sent to email", example = "123456")
        String resetCode,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Schema(description = "New password (min 8 characters)", example = "NewSecurePass123!")
        String newPassword
) {
}

package com.parkmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Refresh token request to get new access token")
public record RefreshRequest(
        @NotBlank(message = "Refresh token is required")
        @Schema(description = "JWT refresh token obtained from login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {
}

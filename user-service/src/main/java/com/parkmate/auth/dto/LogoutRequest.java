package com.parkmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Logout request to invalidate refresh token")
public record LogoutRequest(
        @NotBlank(message = "Refresh token is required")
        @Schema(description = "JWT refresh token to be invalidated", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {
}

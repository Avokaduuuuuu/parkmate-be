package com.parkmate.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccountBasicResponse(
        @Schema(description = "Account unique identifier", example = "10")
        Long id,

        @Schema(description = "Username of the account", example = "john_doe")
        String username,

        @Schema(description = "Email of the account", example = "john@example.com")
        String email
) {
}

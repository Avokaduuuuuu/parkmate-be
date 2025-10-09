package com.parkmate.account.dto;

import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record AccountBasicResponse(
        @Schema(description = "Account unique identifier", example = "10")
        Long id,

        @Schema(description = "Email of the account", example = "john@example.com")
        String email,

        @Schema(description = "Status of the account", example = "ACTIVE")
        AccountStatus status,

        @Schema(description = "Role of the account", example = "USER")
        AccountRole role


) {
}

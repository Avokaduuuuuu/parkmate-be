package com.parkmate.session.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        name = "SessionUpdateRequest",
        description = "Request payload for updating an existing parking session"
)
public record SessionUpdateRequest(
        @Schema(
                description = "Timestamp when the vehicle exited the parking lot (set this to end the session)",
                example = "2025-10-06T16:45:00",
                type = "string",
                format = "date-time",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        LocalDateTime exitTime,

        @Schema(
                description = "Additional notes or comments about the parking session (e.g., issues, special circumstances)",
                example = "Vehicle had difficulty exiting due to payment system issue",
                maxLength = 500,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String note
) {
}
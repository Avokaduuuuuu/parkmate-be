package com.parkmate.policy.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PolicyUpdateRequest(
        @Schema(
                description = """
                        The value in minutes that applies to the policy type:
                        - For EARLY_CHECK_IN_BUFFER: Minutes allowed before scheduled check-in (e.g., 10 = can check in 10 minutes early)
                        - For LATE_CHECK_OUT_BUFFER: Minutes allowed after scheduled check-out (e.g., 15 = can check out 15 minutes late)
                        - For LATE_CHECK_IN_CANCEL_AFTER: Minutes after scheduled check-in before cancellation (e.g., 30 = cancelled if 30+ minutes late)
                        - For EARLY_CANCEL_REFUND_BEFORE: Minutes before check-in to get refund (e.g., 60 = must cancel 1 hour before to get refund)
                        """,
                example = "10",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Value must not be null")
        @Min(value = 0, message = "Value must not be negative")
        Integer value
) {
}

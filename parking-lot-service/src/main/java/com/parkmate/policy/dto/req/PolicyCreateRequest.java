package com.parkmate.policy.dto.req;

import com.parkmate.policy.enums.PolicyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a policy for a parking lot")
public record PolicyCreateRequest(
        @Schema(
                description = """
                        Type of policy this parking lot applies:
                        - EARLY_CHECK_IN_BUFFER: Grace period (in minutes) allowed for early check-in before scheduled time
                        - LATE_CHECK_OUT_BUFFER: Grace period (in minutes) allowed for late check-out after scheduled time
                        - LATE_CHECK_IN_CANCEL_AFTER: Time limit (in minutes) after which a late check-in results in automatic cancellation
                        - EARLY_CANCEL_REFUND_BEFORE: Minimum time (in minutes) before check-in to cancel and receive a refund
                        """,
                example = "EARLY_CHECK_IN_BUFFER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Policy Type must not be null")
        PolicyType policyType,

        @Schema(
                description = """
                        The value in minutes that applies to the policy type:
                        - For EARLY_CHECK_IN_BUFFER: Minutes allowed before scheduled check-in (e.g., 10 = can check in 10 minutes early)
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
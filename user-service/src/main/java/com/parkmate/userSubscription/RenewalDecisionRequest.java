package com.parkmate.userSubscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to set user's subscription renewal decision")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenewalDecisionRequest {

    @NotNull(message = "Renewal decision is required")
    @Schema(
            description = "Whether to continue auto-renewal. true = keep auto-renewal enabled, false = disable auto-renewal",
            example = "true",
            required = true
    )
    private Boolean continueRenewal;
}

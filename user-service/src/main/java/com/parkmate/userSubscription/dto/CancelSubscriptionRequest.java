package com.parkmate.userSubscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to cancel a user subscription")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelSubscriptionRequest {

    @Schema(description = "Reason for cancellation", example = "Không còn nhu cầu sử dụng")
    private String reason;

}

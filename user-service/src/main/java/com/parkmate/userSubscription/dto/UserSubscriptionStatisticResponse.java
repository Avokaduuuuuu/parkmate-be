package com.parkmate.userSubscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Statistic for user subscription")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserSubscriptionStatisticResponse {
    Long subscriptionId;
    Long total;
}

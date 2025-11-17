package com.parkmate.client.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Schema(description = "Statistic for user subscription")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserSubscriptionStatistic {
    Long subscriptionId;
    Long total;
    BigDecimal totalAmount;
}

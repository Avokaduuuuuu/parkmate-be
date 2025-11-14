package com.parkmate.client.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionStatistic {
    BigDecimal totalSubscriptionRevenue;
    List<UserSubscriptionStatistic> userSubscriptionStatistics;
}

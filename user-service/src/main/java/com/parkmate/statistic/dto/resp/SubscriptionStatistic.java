package com.parkmate.statistic.dto.resp;

import com.parkmate.userSubscription.UserSubscription;
import com.parkmate.userSubscription.dto.UserSubscriptionStatisticResponse;
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
    List<UserSubscriptionStatisticResponse> userSubscriptionStatistics;
}

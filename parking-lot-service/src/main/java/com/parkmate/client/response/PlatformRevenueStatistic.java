package com.parkmate.client.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformRevenueStatistic {
    BigDecimal totalOperationalFee;
    Double operationalGrowthRate;
    BigDecimal totalSubscription;
    Double subscriptionGrowthRate;
    BigDecimal totalPlatformRevenue;
    Double platformRevenueGrowthRate;
    BigDecimal totalReservationRevenue;
    Double reservationGrowthRate;
    BigDecimal totalSessionRevenue;
    Double sessionRevenueGrowthRate;
}

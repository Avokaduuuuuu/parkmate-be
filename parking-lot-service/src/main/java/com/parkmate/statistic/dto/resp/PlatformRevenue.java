package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformRevenue {
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

package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SessionStatisticResponse {
    BigDecimal sessionTotalAmount;
    Double sessionTotalAmountGrowthRate;
    BigDecimal memberTotalAmount;
    Double memberTotalAmountGrowthRate;
    BigDecimal occasionalTotalAmount;
    Double occasionalTotalAmountGrowthRate;
    Long completedSessions;
    Long activeSessions;
    Double averageDurationMinute;
    Double averageDurationMinuteGrowthRate;
    Long motorbikeCount;
    Long carCount;
    Long bikeCount;
    Long otherCount;
    Long memberCount;
    Double memberCountGrowthRate;
    Long occasionalCount;
    Double occasionalCountGrowthRate;
}

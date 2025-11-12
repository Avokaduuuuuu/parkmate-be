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
    Long completedSessions;
    Long activeSessions;
    Double averageDurationMinute;
    long motorbikeCount;
    long carCount;
    long bikeCount;
    long otherCount;
}

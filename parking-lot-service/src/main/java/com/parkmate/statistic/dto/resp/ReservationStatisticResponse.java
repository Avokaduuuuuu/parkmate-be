package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationStatisticResponse {
    Long activeCount;
    Long completedCount;
    Long pendingCount;
    Long cancelledCount;
    Long expiredCount;
    BigDecimal totalRevenue;
}

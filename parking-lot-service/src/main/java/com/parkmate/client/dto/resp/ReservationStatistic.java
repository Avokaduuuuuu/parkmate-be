package com.parkmate.client.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationStatistic {
    Long activeCount;
    Long competedCount;
    Long pendingCount;
    Long cancelledCount;
    Long expiredCount;
    BigDecimal totalRevenue;
}

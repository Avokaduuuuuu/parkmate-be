package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemStatisticResponse {
    // parking-lot-service
    BigDecimal systemRevenue;
    Long totalSessions;
    Long totalReservations;
    Long totalSubscriptions;
    Long totalParkingLots;


    // user-service
    Long totalPartners;
    Long totalMembers;
}

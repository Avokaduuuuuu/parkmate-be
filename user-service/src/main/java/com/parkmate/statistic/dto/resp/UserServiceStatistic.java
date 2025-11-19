package com.parkmate.statistic.dto.resp;

import com.parkmate.reservation.dto.ReservationStatisticResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceStatistic {
    ReservationStatisticResponse reservationStatistic;
    SubscriptionStatistic subscriptionStatistic;
}

package com.parkmate.statistic.dto.resp;

import com.parkmate.reservation.dto.ReservationStatisticResponse;
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
public class UserServiceStatistic {
    ReservationStatisticResponse reservationStatistic;
    SubscriptionStatistic subscriptionStatistic;
}

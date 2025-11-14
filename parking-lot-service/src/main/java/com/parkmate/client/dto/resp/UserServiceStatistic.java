package com.parkmate.client.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceStatistic {
    ReservationStatistic reservationStatistic;
    SubscriptionStatistic subscriptionStatistic;
}


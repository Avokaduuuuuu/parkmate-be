package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformOverviewStatistic {
    PlatformRevenue revenue;
    PlatformPartners partners;
    PlatformUsers users;
    PlatformLots lots;
}

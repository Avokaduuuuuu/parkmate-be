package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformUserStatistic {
    Long total;
    Long newThisPeriod;
}

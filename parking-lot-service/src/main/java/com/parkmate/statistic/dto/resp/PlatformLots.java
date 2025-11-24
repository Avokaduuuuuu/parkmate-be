package com.parkmate.statistic.dto.resp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformLots {
    Long total;
    Long activeTotal;
    Long pendingTotal;
    Long underMaintenanceTotal;
    Long preparingTotal;
}

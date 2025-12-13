package com.parkmate.client.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlatformPartnerStatistic {
    Long total;
    Long activeTotal;
    Long suspendedTotal;
    Long pendingRegistrations;
}

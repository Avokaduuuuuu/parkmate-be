package com.parkmate.statistic;

import com.parkmate.statistic.dto.resp.PlatformPartnerStatistic;
import com.parkmate.statistic.dto.resp.PlatformUserStatistic;
import com.parkmate.statistic.dto.resp.UserServiceStatistic;

import java.time.LocalDateTime;

public interface StatisticService {
    UserServiceStatistic getUserStatistic(
            Long lotId,
            LocalDateTime from,
            LocalDateTime to
    );

    PlatformPartnerStatistic getPartnerStatistic();
    PlatformUserStatistic getUserStatistic(LocalDateTime from, LocalDateTime to);
}

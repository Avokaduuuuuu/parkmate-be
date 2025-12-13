package com.parkmate.statistic;

import com.parkmate.statistic.dto.resp.PlatformRevenueStatistic;

import java.time.LocalDateTime;

public interface StatisticService {
    PlatformRevenueStatistic getPlatformRevenueStatistic(
            LocalDateTime from, LocalDateTime to
    );
}

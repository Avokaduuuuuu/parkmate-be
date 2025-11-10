package com.parkmate.statistic;

import com.parkmate.statistic.dto.resp.ParkingLotStatisticResponse;

import java.time.LocalDateTime;

public interface StatisticService {
    ParkingLotStatisticResponse getParkingLotStatistic(Long lotId, LocalDateTime from, LocalDateTime to);
}

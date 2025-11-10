package com.parkmate.statistic;

import java.math.BigDecimal;

public interface ParkingLotStatisticProjection {
    BigDecimal getTotal();
    Long getCompletedCount();
    Long getActiveCount();
}

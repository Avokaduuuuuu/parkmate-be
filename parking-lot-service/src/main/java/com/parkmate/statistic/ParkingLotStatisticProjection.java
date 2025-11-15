package com.parkmate.statistic;

import java.math.BigDecimal;

public interface ParkingLotStatisticProjection {
    BigDecimal getTotal();

    Long getCompletedCount();

    Long getActiveCount();

    Double getAverageDurationMinute();

    Long getMotorbikeCount();

    Long getCarCount();

    Long getBikeCount();

    Long getOtherCount();
}

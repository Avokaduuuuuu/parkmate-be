package com.parkmate.statistic;

import java.math.BigDecimal;

public interface ParkingLotStatisticProjection {
    BigDecimal getTotal();
    long getCompletedCount();
    long getActiveCount();
    double getAverageDurationMinute();
    long getMotorbikeCount();
    long getCarCount();
    long getBikeCount();
    long getOtherCount();
}

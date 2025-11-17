package com.parkmate.statistic;

import java.math.BigDecimal;

public interface ParkingLotStatisticProjection {
    BigDecimal getTotal();
    BigDecimal getMemberTotal();
    BigDecimal getOccasionalTotal();
    Long getCompletedCount();
    Long getActiveCount();
    Double getAverageDurationMinute();
    Long getMotorbikeCount();
    Long getCarCount();
    Long getBikeCount();
    Long getOtherCount();
    Long getMemberCount();
    Long getOccasionalCount();
}

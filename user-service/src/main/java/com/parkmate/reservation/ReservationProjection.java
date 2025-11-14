package com.parkmate.reservation;

import java.math.BigDecimal;

public interface ReservationProjection {
    Long getActiveCount();
    Long getCompletedCount();
    Long getPendingCount();
    Long getCancelledCount();
    Long getExpiredCount();
    BigDecimal getTotalRevenue();
}

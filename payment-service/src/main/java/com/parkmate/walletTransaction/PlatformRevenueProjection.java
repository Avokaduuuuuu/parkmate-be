package com.parkmate.walletTransaction;

import java.math.BigDecimal;

public interface PlatformRevenueProjection {
    BigDecimal getTotalSubscriptionPlatformFee();
    BigDecimal getTotalSessionPlatformFee();
    BigDecimal getTotalReservationPlatformFee();
}

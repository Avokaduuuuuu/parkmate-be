package com.parkmate.userSubscription;

import java.math.BigDecimal;

public interface UserSubscriptionProjection {
    Long getId();
    Long getTotal();
    BigDecimal getTotalAmount();
}

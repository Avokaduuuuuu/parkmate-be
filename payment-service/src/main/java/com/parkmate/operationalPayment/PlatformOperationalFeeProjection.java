package com.parkmate.operationalPayment;

import java.math.BigDecimal;

public interface PlatformOperationalFeeProjection {
    BigDecimal getTotalOperationalFee();
}

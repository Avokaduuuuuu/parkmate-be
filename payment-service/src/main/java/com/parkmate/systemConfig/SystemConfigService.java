package com.parkmate.systemConfig;

import java.math.BigDecimal;

public interface SystemConfigService {
    Integer getWithdrawalCutoffDay();
    BigDecimal getPlatformFeePercentage();
    Integer getOperationalFeeBillingCycleDay();
}

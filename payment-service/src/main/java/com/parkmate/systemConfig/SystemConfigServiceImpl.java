package com.parkmate.systemConfig;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public Integer getWithdrawalCutoffDay() {
        return getConfigAsInteger(ConfigKey.WITHDRAWAL_CUTOFF_DAY);
    }

    @Override
    public BigDecimal getPlatformFeePercentage() {
        return getConfigAsBigDecimal(ConfigKey.PLATFORM_FEE_PERCENTAGE);
    }

    @Override
    public Integer getOperationalFeeBillingCycleDay() {
        return getConfigAsInteger(ConfigKey.OPERATIONAL_FEE_BILLING_CYCLE_DAY);
    }

    private Integer getConfigAsInteger(ConfigKey key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_CONFIG_NOT_FOUND, key.name()));

        try {
            return Integer.parseInt(config.getConfigValue());
        } catch (NumberFormatException e) {
            log.error("Invalid integer config value for {}: {}", key, config.getConfigValue());
            throw new AppException(ErrorCode.INVALID_CONFIG_VALUE, key.name());
        }
    }

    private BigDecimal getConfigAsBigDecimal(ConfigKey key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_CONFIG_NOT_FOUND, key.name()));

        try {
            return new BigDecimal(config.getConfigValue());
        } catch (NumberFormatException e) {
            log.error("Invalid decimal config value for {}: {}", key, config.getConfigValue());
            throw new AppException(ErrorCode.INVALID_CONFIG_VALUE, key.name());
        }
    }
}

package com.parkmate.statistic;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.operationalPayment.OperationalPaymentRepository;
import com.parkmate.operationalPayment.PlatformOperationalFeeProjection;
import com.parkmate.statistic.dto.resp.PlatformRevenueStatistic;
import com.parkmate.systemConfig.ConfigKey;
import com.parkmate.systemConfig.SystemConfig;
import com.parkmate.systemConfig.SystemConfigRepository;
import com.parkmate.walletTransaction.PlatformRevenueProjection;
import com.parkmate.walletTransaction.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final OperationalPaymentRepository operationalPaymentRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final WalletTransactionRepository walletTransactionRepository;


    @Override
    public PlatformRevenueStatistic getPlatformRevenueStatistic(LocalDateTime from, LocalDateTime to) {
        long days = ChronoUnit.DAYS.between(from, to);
        LocalDateTime previousFrom = from.minusDays(days-1);
        LocalDateTime previousTo = to.minusDays(1);

        SystemConfig platformFeeConfig = systemConfigRepository.findByConfigKey(ConfigKey.PLATFORM_FEE_PERCENTAGE)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_CONFIG_NOT_FOUND));
        BigDecimal platformFeeValue = BigDecimal.valueOf(Double.parseDouble(platformFeeConfig.getConfigValue()));

        // Operational Fee
        PlatformOperationalFeeProjection platformOperationalFeeProjection = operationalPaymentRepository.getPlatformOperationalFee(from, to);
        PlatformOperationalFeeProjection previousPlatformOperationalFeeProjection = operationalPaymentRepository.getPlatformOperationalFee(previousFrom, previousTo);

        // Platform Fee
        PlatformRevenueProjection platformRevenueProjection = walletTransactionRepository.getPlatformRevenueStatistic(from, to, platformFeeValue);
        PlatformRevenueProjection previousPlatformrevenueProjection = walletTransactionRepository.getPlatformRevenueStatistic(previousFrom, previousTo, platformFeeValue);

        BigDecimal totalSubscriptionPlatformFee = platformRevenueProjection.getTotalSubscriptionPlatformFee();
        BigDecimal totalReservationPlatformFee = platformRevenueProjection.getTotalReservationPlatformFee();
        BigDecimal totalSessionPlatformFee = platformRevenueProjection.getTotalSessionPlatformFee();

        BigDecimal previousTotalSubscriptionPlatformFee = previousPlatformrevenueProjection.getTotalSubscriptionPlatformFee();
        BigDecimal previousTotalReservationPlatformFee = previousPlatformrevenueProjection.getTotalReservationPlatformFee();
        BigDecimal previousTotalSessionPlatformFee = previousPlatformrevenueProjection.getTotalSessionPlatformFee();

        BigDecimal totalOperationalFee = platformOperationalFeeProjection.getTotalOperationalFee();
        BigDecimal previousTotalOperationalFee = previousPlatformOperationalFeeProjection.getTotalOperationalFee();

        // All values are guaranteed non-null by COALESCE in queries, but add safety checks for consistency
        BigDecimal totalPlatformRevenue = safeAdd(totalReservationPlatformFee,
                safeAdd(totalSessionPlatformFee,
                        safeAdd(totalSubscriptionPlatformFee, totalOperationalFee)));

        BigDecimal totalPreviousPlatformRevenue = safeAdd(previousTotalReservationPlatformFee,
                safeAdd(previousTotalSubscriptionPlatformFee,
                        safeAdd(previousTotalSessionPlatformFee, previousTotalOperationalFee)));

        return PlatformRevenueStatistic.builder()
                .totalOperationalFee(platformOperationalFeeProjection.getTotalOperationalFee())
                .operationalGrowthRate(getGrowthRate(platformOperationalFeeProjection.getTotalOperationalFee(), previousPlatformOperationalFeeProjection.getTotalOperationalFee()))
                .totalSubscription(totalSubscriptionPlatformFee)
                .subscriptionGrowthRate(getGrowthRate(totalSubscriptionPlatformFee, previousTotalSubscriptionPlatformFee))
                .totalReservationRevenue(totalReservationPlatformFee)
                .reservationGrowthRate(getGrowthRate(totalReservationPlatformFee, previousTotalReservationPlatformFee))
                .totalSessionRevenue(totalSessionPlatformFee)
                .sessionRevenueGrowthRate(getGrowthRate(totalSessionPlatformFee, previousTotalSessionPlatformFee))
                .totalPlatformRevenue(totalPlatformRevenue)
                .platformRevenueGrowthRate(getGrowthRate(totalPlatformRevenue, totalPreviousPlatformRevenue))
                .build();
    }

    private Double getGrowthRate(BigDecimal current, BigDecimal previous) {
        if (current == null) {
            return BigDecimal.ZERO.doubleValue();
        }
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) > 0) {
                return 100.0;
            }
            return 0.0;
        }
        BigDecimal difference = current.subtract(previous);
        BigDecimal rate = difference.divide(previous, 4, RoundingMode.HALF_UP);
        BigDecimal percentageRate = rate.multiply(BigDecimal.valueOf(100));
        return percentageRate.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        BigDecimal safeA = a != null ? a : BigDecimal.ZERO;
        BigDecimal safeB = b != null ? b : BigDecimal.ZERO;
        return safeA.add(safeB);
    }
}

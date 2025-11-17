package com.parkmate.statistic;

import com.parkmate.client.UserClient;
import com.parkmate.client.response.UserServiceStatistic;
import com.parkmate.client.response.UserSubscriptionStatistic;
import com.parkmate.session.SessionRepository;
import com.parkmate.statistic.dto.resp.*;
import com.parkmate.subscription.SubscriptionEntity;
import com.parkmate.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final SessionRepository sessionRepository;
    private final UserClient userClient;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public ParkingLotStatisticResponse getParkingLotStatistic(Long lotId, LocalDateTime from, LocalDateTime to) {
        ParkingLotStatisticProjection cursor =  sessionRepository.getParkingLotStatistic(lotId, from, to);
        long days = ChronoUnit.DAYS.between(from, to);
        LocalDateTime previousFrom = from.minusDays(days-1);
        LocalDateTime previousTo = to.minusDays(1);
        ParkingLotStatisticProjection previousCursor = sessionRepository.getParkingLotStatistic(lotId, previousFrom, previousTo);
        UserServiceStatistic userServiceStatistic = userClient.getUserRevenueStatistic(lotId, from, to).getData();
        UserServiceStatistic previousUserServiceStatistic = userClient.getUserRevenueStatistic(lotId, previousFrom, previousTo).getData();


        ReservationStatisticResponse reservationStatisticResponse = ReservationStatisticResponse.builder()
                .activeCount(userServiceStatistic.getReservationStatistic().getActiveCount())
                .completedCount(userServiceStatistic.getReservationStatistic().getCompetedCount())
                .pendingCount(userServiceStatistic.getReservationStatistic().getPendingCount())
                .cancelledCount(userServiceStatistic.getReservationStatistic().getCancelledCount())
                .expiredCount(userServiceStatistic.getReservationStatistic().getExpiredCount())
                .totalRevenue(userServiceStatistic.getReservationStatistic().getTotalRevenue())
                .totalRevenueGrowthRage(getGrowthRate(userServiceStatistic.getReservationStatistic().getTotalRevenue(), previousUserServiceStatistic.getReservationStatistic().getTotalRevenue()))
                .build();


        Map<Long, String> subscriptionNameMap = subscriptionRepository.findAllByParkingLotId(lotId)
                .stream()
                .collect(Collectors.toMap(SubscriptionEntity::getId, SubscriptionEntity::getName));

        Map<Long, Long> userSubscriptionMap = userServiceStatistic.getSubscriptionStatistic().getUserSubscriptionStatistics()
                .stream()
                .collect(Collectors.toMap(UserSubscriptionStatistic::getSubscriptionId, UserSubscriptionStatistic::getTotal));

        Map<Long, BigDecimal> userSubscriptionAmountMap =
                userServiceStatistic.getSubscriptionStatistic().getUserSubscriptionStatistics()
                        .stream()
                        .collect(Collectors.toMap(
                                UserSubscriptionStatistic::getSubscriptionId,
                                UserSubscriptionStatistic::getTotalAmount
                        ));


        SubscriptionStatisticResponse subscriptionStatisticResponse = SubscriptionStatisticResponse.builder()
                .totalRevenue(userServiceStatistic.getSubscriptionStatistic().getTotalSubscriptionRevenue())
                .userSubscriptionStatistics(buildUserSubscriptionStatistics(subscriptionNameMap, userSubscriptionMap, userSubscriptionAmountMap))
                .totalRevenueGrowthRate(getGrowthRate(userServiceStatistic.getSubscriptionStatistic().getTotalSubscriptionRevenue(), previousUserServiceStatistic.getSubscriptionStatistic().getTotalSubscriptionRevenue()))
                .build();


        return ParkingLotStatisticResponse.builder()
                .sessionStatistic(SessionStatisticResponse.builder()
                        .sessionTotalAmount(cursor.getTotal())
                        .sessionTotalAmountGrowthRate(getGrowthRate(cursor.getTotal(), previousCursor.getTotal()))
                        .memberTotalAmount(cursor.getMemberTotal())
                        .memberTotalAmountGrowthRate(getGrowthRate(cursor.getMemberTotal(), previousCursor.getMemberTotal()))
                        .occasionalTotalAmount(cursor.getOccasionalTotal())
                        .occasionalTotalAmountGrowthRate((getGrowthRate(cursor.getOccasionalTotal(), previousCursor.getOccasionalTotal())))
                        .completedSessions((cursor.getCompletedCount()))
                        .activeSessions((cursor.getActiveCount()))
                        .averageDurationMinute(cursor.getAverageDurationMinute())
                        .averageDurationMinuteGrowthRate(getGrowthRate(BigDecimal.valueOf(cursor.getAverageDurationMinute()), BigDecimal.valueOf(previousCursor.getAverageDurationMinute())))
                        .motorbikeCount(cursor.getMotorbikeCount())
                        .carCount(cursor.getCarCount())
                        .bikeCount(cursor.getBikeCount())
                        .otherCount(cursor.getOtherCount())
                        .memberCount(cursor.getMemberCount())
                        .memberCountGrowthRate(getGrowthRate(BigDecimal.valueOf(cursor.getMemberCount()), BigDecimal.valueOf(previousCursor.getMemberCount())))
                        .occasionalCount(cursor.getOccasionalCount())
                        .occasionalCountGrowthRate(getGrowthRate(BigDecimal.valueOf(cursor.getOccasionalCount()), BigDecimal.valueOf(previousCursor.getOccasionalCount())))
                        .build()
                )
                .reservationStatistic(reservationStatisticResponse)
                .subscriptionStatistic(subscriptionStatisticResponse)
                .build();
    }

    private List<UserSubscriptionStatisticResponse> buildUserSubscriptionStatistics(
            Map<Long, String> subscriptionNameMap,
            Map<Long, Long> userSubscriptionMap,
            Map<Long, BigDecimal> userSubscriptionAmountMap
    ) {
        return subscriptionNameMap.entrySet().stream()
                .map(entry -> UserSubscriptionStatisticResponse.builder()
                        .packageName(entry.getValue())
                        .total(userSubscriptionMap.getOrDefault(entry.getKey(), 0L))
                        .totalAmount(userSubscriptionAmountMap.getOrDefault(entry.getKey(), BigDecimal.ZERO))
                        .build())
                .toList();
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
}

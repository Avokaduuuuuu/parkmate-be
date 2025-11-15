package com.parkmate.statistic;

import com.parkmate.client.UserClient;
import com.parkmate.client.dto.resp.UserServiceStatistic;
import com.parkmate.client.dto.resp.UserSubscriptionStatistic;
import com.parkmate.session.SessionRepository;
import com.parkmate.statistic.dto.resp.*;
import com.parkmate.subscription.SubscriptionEntity;
import com.parkmate.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        ParkingLotStatisticProjection cursor = sessionRepository.getParkingLotStatistic(lotId, from, to);
        UserServiceStatistic userServiceStatistic = userClient.getUserRevenueStatistic(lotId, from, to).getData();


        ReservationStatisticResponse reservationStatisticResponse = ReservationStatisticResponse.builder()
                .activeCount(userServiceStatistic.getReservationStatistic().getActiveCount())
                .completedCount(userServiceStatistic.getReservationStatistic().getCompetedCount())
                .pendingCount(userServiceStatistic.getReservationStatistic().getPendingCount())
                .cancelledCount(userServiceStatistic.getReservationStatistic().getCancelledCount())
                .expiredCount(userServiceStatistic.getReservationStatistic().getExpiredCount())
                .totalRevenue(userServiceStatistic.getReservationStatistic().getTotalRevenue())
                .build();

        Map<Long, String> subscriptionNameMap = subscriptionRepository.findAllByParkingLotId(lotId)
                .stream()
                .collect(Collectors.toMap(SubscriptionEntity::getId, SubscriptionEntity::getName));

        Map<Long, Long> userSubscriptionMap = userServiceStatistic.getSubscriptionStatistic().getUserSubscriptionStatistics()
                .stream()
                .collect(Collectors.toMap(UserSubscriptionStatistic::getSubscriptionId, UserSubscriptionStatistic::getTotal));


        SubscriptionStatisticResponse subscriptionStatisticResponse = SubscriptionStatisticResponse.builder()
                .totalRevenue(userServiceStatistic.getSubscriptionStatistic().getTotalSubscriptionRevenue())
                .userSubscriptionStatistics(buildUserSubscriptionStatistics(subscriptionNameMap, userSubscriptionMap))
                .build();


        return ParkingLotStatisticResponse.builder()
                .sessionStatistic(SessionStatisticResponse.builder()
                        .sessionTotalAmount(cursor.getTotal())
                        .completedSessions((cursor.getCompletedCount()))
                        .activeSessions((cursor.getActiveCount()))
                        .averageDurationMinute(cursor.getAverageDurationMinute())
                        .motorbikeCount(cursor.getMotorbikeCount())
                        .carCount(cursor.getCarCount())
                        .bikeCount(cursor.getBikeCount())
                        .otherCount(cursor.getOtherCount())
                        .build()
                )
                .reservationStatistic(reservationStatisticResponse)
                .subscriptionStatistic(subscriptionStatisticResponse)
                .build();
    }

    private List<UserSubscriptionStatisticResponse> buildUserSubscriptionStatistics(
            Map<Long, String> subscriptionNameMap,
            Map<Long, Long> userSubscriptionMap
    ) {
        return subscriptionNameMap.entrySet().stream()
                .map(entry -> UserSubscriptionStatisticResponse.builder()
                        .packageName(entry.getValue())
                        .total(userSubscriptionMap.getOrDefault(entry.getKey(), 0L))
                        .build())
                .toList();
    }

}

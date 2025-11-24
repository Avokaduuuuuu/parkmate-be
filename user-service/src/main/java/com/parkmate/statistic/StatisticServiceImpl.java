package com.parkmate.statistic;

import com.parkmate.common.enums.RequestStatus;
import com.parkmate.partner.PartnerRepository;
import com.parkmate.partner.PlatformPartnerProjection;
import com.parkmate.partnerRegistration.PartnerRegistrationRepository;
import com.parkmate.reservation.ReservationProjection;
import com.parkmate.reservation.ReservationRepository;
import com.parkmate.reservation.dto.ReservationStatisticResponse;
import com.parkmate.statistic.dto.resp.PlatformPartnerStatistic;
import com.parkmate.statistic.dto.resp.PlatformUserStatistic;
import com.parkmate.statistic.dto.resp.SubscriptionStatistic;
import com.parkmate.statistic.dto.resp.UserServiceStatistic;
import com.parkmate.user.PlatformUserProjection;
import com.parkmate.user.UserRepository;
import com.parkmate.userSubscription.UserSubscriptionProjection;
import com.parkmate.userSubscription.UserSubscriptionRepository;
import com.parkmate.userSubscription.dto.UserSubscriptionStatisticResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final ReservationRepository reservationRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerRegistrationRepository partnerRegistrationRepository;
    private final UserRepository userRepository;

    @Override
    public UserServiceStatistic getUserStatistic(
            Long lotId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        ReservationProjection reservationStatistic = reservationRepository.getStatistic(lotId, from, to);
        ReservationStatisticResponse reservationStatisticResponse = ReservationStatisticResponse.builder()
                .activeCount(reservationStatistic.getActiveCount())
                .competedCount(reservationStatistic.getCompletedCount())
                .cancelledCount(reservationStatistic.getCancelledCount())
                .expiredCount(reservationStatistic.getExpiredCount())
                .pendingCount(reservationStatistic.getPendingCount())
                .totalRevenue(reservationStatistic.getTotalRevenue())
                .build();

        BigDecimal totalSubscriptionRevenue = userSubscriptionRepository.getTotalRevenue(lotId, from, to);
        List<UserSubscriptionProjection> userSubscriptionProjections = userSubscriptionRepository.getUserSubscriptionStatistic(lotId, from, to);
        List<UserSubscriptionStatisticResponse> userSubscriptionStatisticResponses = userSubscriptionProjections.stream()
                .map(usp -> UserSubscriptionStatisticResponse.builder()
                        .subscriptionId(usp.getId())
                        .total(usp.getTotal())
                        .totalAmount(usp.getTotalAmount())
                        .build())
                .toList();

        return UserServiceStatistic.builder()
                .reservationStatistic(reservationStatisticResponse)
                .subscriptionStatistic(
                        SubscriptionStatistic.builder()
                                .totalSubscriptionRevenue(totalSubscriptionRevenue)
                                .userSubscriptionStatistics(userSubscriptionStatisticResponses)
                                .build()
                )
                .build();
    }

    @Override
    public PlatformPartnerStatistic getPartnerStatistic() {
        PlatformPartnerProjection platformPartnerProjection = partnerRepository.getPlatformPartnerStatistic();
        return PlatformPartnerStatistic.builder()
                .total(platformPartnerProjection.getTotal())
                .activeTotal(platformPartnerProjection.getActiveTotal())
                .suspendedTotal(platformPartnerProjection.getSuspendedTotal())
                .pendingRegistrations(partnerRegistrationRepository.countPartnerRegistrationByStatus(RequestStatus.PENDING))
                .build();
    }

    @Override
    public PlatformUserStatistic getUserStatistic(LocalDateTime from, LocalDateTime to) {
        PlatformUserProjection platformUserProjection = userRepository.getPlatformUserStatistic(from, to);
        return PlatformUserStatistic.builder()
                .total(platformUserProjection.getTotal())
                .newThisPeriod(platformUserProjection.getNewThisPeriod())
                .build();
    }
}

package com.parkmate.statistic;

import com.parkmate.session.SessionRepository;
import com.parkmate.statistic.dto.resp.ParkingLotStatisticResponse;
import com.parkmate.statistic.dto.resp.SessionStatisticResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final SessionRepository sessionRepository;

    @Override
    public ParkingLotStatisticResponse getParkingLotStatistic(Long lotId, LocalDateTime from, LocalDateTime to) {
        ParkingLotStatisticProjection cursor =  sessionRepository.getParkingLotStatistic(lotId, from, to);
        return ParkingLotStatisticResponse.builder()
                .sessionStatistic(SessionStatisticResponse.builder()
                        .sessionTotalAmount(cursor.getTotal())
                        .completedSessions((cursor.getCompletedCount()))
                        .activeSessions((cursor.getActiveCount()))
                        .build()
                )
                .build();
    }
}

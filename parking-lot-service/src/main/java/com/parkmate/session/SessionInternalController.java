package com.parkmate.session;

import com.parkmate.common.ApiResponse;
import com.parkmate.session.dto.RevenueWithCount;
import com.parkmate.session.enums.ReferenceType;
import com.parkmate.session.enums.SessionType;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Internal controller for session-related operations
 * Used by payment-service to calculate partner withdrawal periods
 */
@RestController
@RequestMapping("/internal/sessions")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class SessionInternalController {

    private final SessionRepository sessionRepository;

    @GetMapping("/revenue/member-reservation")
    public ApiResponse<RevenueWithCount> getMemberReservationRevenue(
            @RequestParam Long lotId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("Getting MEMBER+RESERVATION revenue and count for lot {} from {} to {}", lotId, from, to);

        try {
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            BigDecimal revenue = sessionRepository.getRevenueByTypeAndReference(
                    lotId,
                    SessionType.MEMBER,
                    ReferenceType.RESERVATION,
                    fromDate,
                    toDate
            );

            Long count = sessionRepository.getCountByTypeAndReference(
                    lotId,
                    SessionType.MEMBER,
                    ReferenceType.RESERVATION,
                    fromDate,
                    toDate
            );

            RevenueWithCount result = RevenueWithCount.builder()
                    .revenue(revenue)
                    .count(count)
                    .build();

            log.debug("MEMBER+RESERVATION for lot {}: revenue={}, count={}", lotId, revenue, count);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Error getting MEMBER+RESERVATION data for lot {}", lotId, e);
            return ApiResponse.success(RevenueWithCount.builder()
                    .revenue(BigDecimal.ZERO)
                    .count(0L)
                    .build());
        }
    }

    @GetMapping("/revenue/member-walkin")
    public ApiResponse<RevenueWithCount> getMemberWalkInRevenue(
            @RequestParam Long lotId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("Getting MEMBER+WALK_IN revenue and count for lot {} from {} to {}", lotId, from, to);

        try {
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            BigDecimal revenue = sessionRepository.getRevenueByTypeAndReference(
                    lotId,
                    SessionType.MEMBER,
                    ReferenceType.WALK_IN,
                    fromDate,
                    toDate
            );

            Long count = sessionRepository.getCountByTypeAndReference(
                    lotId,
                    SessionType.MEMBER,
                    ReferenceType.WALK_IN,
                    fromDate,
                    toDate
            );

            RevenueWithCount result = RevenueWithCount.builder()
                    .revenue(revenue)
                    .count(count)
                    .build();

            log.debug("MEMBER+WALK_IN for lot {}: revenue={}, count={}", lotId, revenue, count);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Error getting MEMBER+WALK_IN data for lot {}", lotId, e);
            return ApiResponse.success(RevenueWithCount.builder()
                    .revenue(BigDecimal.ZERO)
                    .count(0L)
                    .build());
        }
    }
}

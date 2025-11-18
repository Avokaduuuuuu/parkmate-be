package com.parkmate.session;

import com.parkmate.common.ApiResponse;
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
    public ApiResponse<BigDecimal> getMemberReservationRevenue(
            @RequestParam Long lotId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("Getting MEMBER+RESERVATION revenue for lot {} from {} to {}", lotId, from, to);

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

            log.debug("MEMBER+RESERVATION revenue for lot {}: {}", lotId, revenue);
            return ApiResponse.success(revenue);

        } catch (Exception e) {
            log.error("Error getting MEMBER+RESERVATION revenue for lot {}", lotId, e);
            return ApiResponse.success(BigDecimal.ZERO);
        }
    }

    @GetMapping("/revenue/member-walkin")
    public ApiResponse<BigDecimal> getMemberWalkInRevenue(
            @RequestParam Long lotId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("Getting MEMBER+WALK_IN revenue for lot {} from {} to {}", lotId, from, to);

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

            log.debug("MEMBER+WALK_IN revenue for lot {}: {}", lotId, revenue);
            return ApiResponse.success(revenue);

        } catch (Exception e) {
            log.error("Error getting MEMBER+WALK_IN revenue for lot {}", lotId, e);
            return ApiResponse.success(BigDecimal.ZERO);
        }
    }
}

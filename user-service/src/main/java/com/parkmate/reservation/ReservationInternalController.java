package com.parkmate.reservation;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.reservation.dto.RevenueWithCount;
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

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class ReservationInternalController {

    private final ReservationRepository reservationRepository;

    /**
     * Get penalty revenue from cancelled/expired reservations
     * This is used for partner withdrawal period calculation
     *
     * @param lotId Parking lot ID
     * @param from Start date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @param to End date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @return Revenue and count from cancelled/expired reservations
     */
    @GetMapping("/penalty-revenue")
    public ApiResponse<RevenueWithCount> getPenaltyRevenue(
            @RequestParam Long lotId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        log.info("Getting penalty revenue for lot {} from {} to {}", lotId, from, to);

        try {
            LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            BigDecimal revenue = reservationRepository.getTotalPenaltyRevenue(lotId, fromDate, toDate);
            Long count = reservationRepository.getTotalPenaltyCount(lotId, fromDate, toDate);

            RevenueWithCount result = RevenueWithCount.builder()
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .count(count != null ? count : 0L)
                    .build();

            log.debug("Penalty revenue for lot {}: revenue={}, count={}", lotId, revenue, count);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Error getting penalty revenue for lot {}", lotId, e);
            return ApiResponse.success(RevenueWithCount.builder()
                    .revenue(BigDecimal.ZERO)
                    .count(0L)
                    .build());
        }
    }
}

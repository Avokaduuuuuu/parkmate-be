package com.parkmate.partnerWithdrawalPeriod;

import com.parkmate.partnerWithdrawalPeriod.dto.response.PartnerWithdrawalPeriodResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledPartnerWithdrawalPeriod {

    private final PartnerWithdrawalPeriodService partnerWithdrawalPeriodService;

    /**
     * Scheduled task to create monthly withdrawal periods for all partners
     * Runs at 1:00 AM on the 1st day of every month (Vietnam timezone)
     *
     * Cron expression: "0 0 1 1 * ?"
     * - Second: 0
     * - Minute: 0
     * - Hour: 1 (1:00 AM)
     * - Day of month: 1 (first day)
     * - Month: * (every month)
     * - Day of week: ? (any day)
     */
    @Scheduled(cron = "0 * * * * ?", zone = "Asia/Ho_Chi_Minh")
    public void createPartnerWithdrawalPeriod() {
        log.info("=== Monthly scheduled task started: Creating partner withdrawal periods ===");

        try {
            List<PartnerWithdrawalPeriodResponse> responses =
                    partnerWithdrawalPeriodService.createMonthlyWithdrawalPeriods();

            log.info("=== Successfully created {} withdrawal periods ===", responses.size());

            // Log summary
            responses.forEach(response -> {
                log.info("Lot {} (Partner {}): Reservation={}, WalkIn={}, Subscription={}, Gross={}, Fee={}, Net={}, Period={}",
                        response.getLotId(),
                        response.getPartnerId(),
                        response.getReservationRevenue(),
                        response.getWalkInRevenue(),
                        response.getSubscriptionRevenue(),
                        response.getGrossRevenue(),
                        response.getPlatformFee(),
                        response.getNetRevenue(),
                        response.getPeriodLabel()
                );
            });

        } catch (Exception e) {
            log.error("=== Failed to create monthly withdrawal periods ===", e);
        }
    }

}

package com.parkmate.payos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledPayoutChecker {

    private final PayOSService payOSService;

    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void checkPendingPayouts() {
        try {
            log.info("Starting scheduled payout check...");
            payOSService.checkPendingPayouts();
            log.info("Completed scheduled payout check");
        } catch (Exception e) {
            log.error("Error during scheduled payout check", e);
        }
    }
}

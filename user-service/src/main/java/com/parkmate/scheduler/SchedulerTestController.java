package com.parkmate.scheduler;

import com.parkmate.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-service/internal/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerTestController {

    private final SchedulerServiceImpl schedulerService;

    @PostMapping("/subscription-expiration-reminder")
    public ApiResponse<String> testSubscriptionExpirationReminder() {
        log.info(">>> Manual trigger: sendSubscriptionExpirationReminders");
        schedulerService.sendSubscriptionExpirationReminders();
        return ApiResponse.success("Subscription expiration reminder triggered");
    }

    @PostMapping("/balance-check")
    public ApiResponse<String> testBalanceCheck() {
        log.info(">>> Manual trigger: checkBalanceForExpiringSubscriptions");
        schedulerService.checkBalanceForExpiringSubscriptions();
        return ApiResponse.success("Balance check for expiring subscriptions triggered");
    }

    @PostMapping("/auto-renewal")
    public ApiResponse<String> testAutoRenewal() {
        log.info(">>> Manual trigger: processSubscriptionAutoRenewal");
        schedulerService.processSubscriptionAutoRenewal();
        return ApiResponse.success("Auto-renewal process triggered");
    }

    @PostMapping("/reservation-reminder")
    public ApiResponse<String> testReservationReminder() {
        log.info(">>> Manual trigger: sendUpcomingReservationReminders");
        schedulerService.sendUpcomingReservationReminders();
        return ApiResponse.success("Reservation reminder triggered");
    }

    @PostMapping("/no-show-detection")
    public ApiResponse<String> testNoShowDetection() {
        log.info(">>> Manual trigger: detectNoShowReservations");
        schedulerService.detectNoShowReservations();
        return ApiResponse.success("No-show detection triggered");
    }
}
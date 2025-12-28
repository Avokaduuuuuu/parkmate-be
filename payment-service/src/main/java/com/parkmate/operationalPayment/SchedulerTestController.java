package com.parkmate.operationalPayment;

import com.parkmate.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-service/internal/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerTestController {

    private final RecurringOperationalPaymentScheduler recurringScheduler;
    private final OperationalPaymentReminderScheduler reminderScheduler;

    @PostMapping("/recurring-payment")
    public ResponseEntity<ApiResponse<String>> testRecurringPayment() {
        log.info(">>> Manual trigger: createRecurringOperationalPayments");
        recurringScheduler.createRecurringOperationalPayments();
        return ResponseEntity.ok(ApiResponse.success(
                "Recurring operational payment creation triggered"
        ));
    }

    @PostMapping("/payment-reminder")
    public ResponseEntity<ApiResponse<String>> testPaymentReminder() {
        log.info(">>> Manual trigger: sendPaymentReminders");
        reminderScheduler.sendPaymentReminders();
        return ResponseEntity.ok(ApiResponse.success(
                "Payment reminder triggered"
        ));
    }

    @PostMapping("/overdue-check")
    public ResponseEntity<ApiResponse<String>> testOverdueCheck() {
        log.info(">>> Manual trigger: markOverduePayments");
        reminderScheduler.markOverduePayments();
        return ResponseEntity.ok(ApiResponse.success(
                "Overdue payment check triggered"
        ));
    }
}

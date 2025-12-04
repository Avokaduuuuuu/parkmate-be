package com.parkmate.operationalPayment;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.UserServiceClient;
import com.parkmate.kafka.publisher.OperationalPaymentNotificationPublisher;
import com.parkmate.operationalPayment.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OperationalPaymentReminderScheduler {

    private final OperationalPaymentRepository operationalPaymentRepository;
    private final OperationalPaymentNotificationPublisher notificationPublisher;
    private final UserServiceClient userServiceClient;
    private final ParkingLotClient parkingLotClient;

    /**
     * Task 1: Send payment reminder 3 days before due date
     * Runs daily at 9:00 AM Vietnam time
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional(readOnly = true)
    public void sendPaymentReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(3); // 3 days from now

        log.info("=== [SCHEDULER] Task 1: Checking for operational payments due on {} ===", reminderDate);

        try {
            List<OperationalPaymentEntity> pendingPayments =
                    operationalPaymentRepository.findPendingPaymentsDueSoon(
                            PaymentStatus.PENDING,
                            reminderDate
                    );

            log.info("Found {} pending operational payments approaching due date", pendingPayments.size());

            for (OperationalPaymentEntity payment : pendingPayments) {
                try {
                    sendReminderEmail(payment);
                } catch (Exception e) {
                    log.error("Failed to send reminder for payment {}", payment.getId(), e);
                }
            }

            log.info("✅ [SCHEDULER] Task 1: Completed processing {} payment reminders", pendingPayments.size());
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Task 1: Fatal error in sendPaymentReminders", e);
        }
    }

    /**
     * Task 2: Mark overdue payments and send overdue notifications
     * Runs daily at 10:00 AM Vietnam time
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void markOverduePayments() {
        LocalDate today = LocalDate.now();

        log.info("=== [SCHEDULER] Task 2: Checking for overdue operational payments as of {} ===", today);

        try {
            List<OperationalPaymentEntity> overduePayments =
                    operationalPaymentRepository.findOverduePayments(
                            PaymentStatus.PENDING,
                            today
                    );

            log.info("Found {} overdue operational payments", overduePayments.size());

            int markedCount = 0;
            for (OperationalPaymentEntity payment : overduePayments) {
                try {
                    // Update status to OVERDUE
                    payment.setPaymentStatus(PaymentStatus.OVERDUE);
                    payment.setNotes("Payment marked as overdue on " + LocalDateTime.now().plusHours(7));
                    operationalPaymentRepository.save(payment);
                    markedCount++;

                    // Send overdue notification
                    sendOverdueNotification(payment);

                } catch (Exception e) {
                    log.error("Failed to mark payment {} as overdue", payment.getId(), e);
                }
            }

            log.info("✅ [SCHEDULER] Task 2: Marked {} payments as OVERDUE", markedCount);
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Task 2: Fatal error in markOverduePayments", e);
        }
    }

    private void sendReminderEmail(OperationalPaymentEntity payment) {
        log.info("Sending payment reminder for payment {} to partner {}",
                payment.getId(), payment.getPartnerId());

        try {
            // Fetch partner email
            var partnerResponse = userServiceClient.getUserById(payment.getPartnerId());
            if (partnerResponse == null || partnerResponse.data() == null) {
                log.warn("Partner {} not found, skipping reminder", payment.getPartnerId());
                return;
            }
            String partnerEmail = partnerResponse.data().getEmail();

            // Fetch parking lot name
            var lotResponse = parkingLotClient.getParkingLotName(payment.getLotId());
            String parkingLotName = (lotResponse != null && lotResponse.data() != null)
                    ? lotResponse.data().name()
                    : "Bãi xe #" + payment.getLotId();

            // Construct payment URL from PayOS
            String paymentUrl = "https://parkmate.vn/partner/payments/" + payment.getId();

            // Publish reminder event
            notificationPublisher.publishPaymentReminder(
                    payment.getPartnerId(),
                    partnerEmail,
                    parkingLotName,
                    payment.getTotalFee(),
                    payment.getDueDate().atStartOfDay(),
                    paymentUrl
            );

            log.info("✅ Sent payment reminder email to {}", partnerEmail);
        } catch (Exception e) {
            log.error("Failed to send reminder email for payment {}", payment.getId(), e);
        }
    }

    private void sendOverdueNotification(OperationalPaymentEntity payment) {
        log.info("Sending overdue notification for payment {} to partner {}",
                payment.getId(), payment.getPartnerId());

        try {
            // Fetch partner email
            var partnerResponse = userServiceClient.getUserById(payment.getPartnerId());
            if (partnerResponse == null || partnerResponse.data() == null) {
                log.warn("Partner {} not found, skipping overdue notification", payment.getPartnerId());
                return;
            }
            String partnerEmail = partnerResponse.data().getEmail();

            // Fetch parking lot name
            var lotResponse = parkingLotClient.getParkingLotName(payment.getLotId());
            String parkingLotName = (lotResponse != null && lotResponse.data() != null)
                    ? lotResponse.data().name()
                    : "Bãi xe #" + payment.getLotId();

            // Publish overdue event
            notificationPublisher.publishPaymentOverdue(
                    payment.getPartnerId(),
                    partnerEmail,
                    parkingLotName,
                    payment.getTotalFee(),
                    payment.getDueDate().atStartOfDay()
            );

            log.info("✅ Sent overdue notification email to {}", partnerEmail);
        } catch (Exception e) {
            log.error("Failed to send overdue notification for payment {}", payment.getId(), e);
        }
    }
}

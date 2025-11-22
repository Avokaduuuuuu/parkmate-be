package com.parkmate.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.PaymentClient;
import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.event.NotificationEventType;
import com.parkmate.mobileDevice.MobileDeviceRepository;
import com.parkmate.reservation.Reservation;
import com.parkmate.reservation.ReservationMapper;
import com.parkmate.reservation.ReservationRepository;
import com.parkmate.user.User;
import com.parkmate.userSubscription.UserSubscription;
import com.parkmate.userSubscription.UserSubscriptionRepository;
import com.parkmate.userSubscription.UserSubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl {

    private final ReservationRepository reservationRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final ParkingLotClient parkingLotClient;
    private final PaymentClient paymentClient;
    private final ReservationMapper reservationMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm");

    @Scheduled(fixedRate = 300000, zone = "Asia/Ho_Chi_Minh") // Every 5 minutes
    @Transactional(readOnly = true)
    public void sendUpcomingReservationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderStart = now.plusMinutes(55); // 55 min from now
        LocalDateTime reminderEnd = now.plusMinutes(65);   // 65 min from now

        log.info("📅 [SCHEDULER] Task 1: Checking for reservations between {} and {}",
                reminderStart.format(DATETIME_FORMATTER),
                reminderEnd.format(DATETIME_FORMATTER));

        try {
            List<Reservation> upcomingReservations = reservationRepository.findByStatusAndReservedFromBetween(
                    ReservationStatus.PENDING,
                    reminderStart,
                    reminderEnd
            );

            log.info("📅 [SCHEDULER] Task 1: Found {} upcoming reservations", upcomingReservations.size());

            for (Reservation reservation : upcomingReservations) {
                try {
                    sendReservationReminder(reservation);
                } catch (Exception e) {
                    log.error("❌ [SCHEDULER] Task 1: Error sending reminder for reservation {}",
                            reservation.getId(), e);
                }
            }

            log.info("✅ [SCHEDULER] Task 1: Completed processing {} reservations", upcomingReservations.size());
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Task 1: Fatal error in sendUpcomingReservationReminders", e);
        }
    }

    @Scheduled(fixedRate = 300000, zone = "Asia/Ho_Chi_Minh") // Every 5 minutes
    @Transactional
    public void detectNoShowReservations() {
        LocalDateTime now = LocalDateTime.now();

        log.info("🚫 [SCHEDULER] Task 2: Starting no-show detection at {}", now.format(DATETIME_FORMATTER));

        try {
            // Get all parking lots with PENDING reservations
            List<Reservation> pendingReservations = reservationRepository.findAll()
                    .stream()
                    .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                    .toList();

            log.info("🚫 [SCHEDULER] Task 2: Found {} PENDING reservations to check", pendingReservations.size());

            int noShowCount = 0;

            for (Reservation reservation : pendingReservations) {
                try {
                    // Fetch grace period policy from parking-lot-service
                    // Default to 15 minutes if policy fetch fails
                    int gracePeriodMinutes = 15;

                    try {
                        // TODO: Implement policy fetch from parking-lot-service
                        // For now, using default 15 minutes
                        // var policyResponse = parkingLotClient.getPolicy(reservation.getParkingLotId(), "NO_SHOW_GRACE_PERIOD");
                    } catch (Exception e) {
                        log.warn("⚠️ [SCHEDULER] Task 2: Failed to fetch grace period policy for lot {}, using default 15 minutes",
                                reservation.getParkingLotId());
                    }

                    LocalDateTime graceDeadline = reservation.getReservedFrom().plusMinutes(gracePeriodMinutes);

                    if (now.isAfter(graceDeadline)) {
                        // Mark as EXPIRED (no-show)
                        reservation.setStatus(ReservationStatus.EXPIRED);
                        reservation.setUpdatedAt(now);
                        reservationRepository.save(reservation);

                        noShowCount++;

                        log.info("🚫 [SCHEDULER] Task 2: Marked reservation {} as EXPIRED (no-show). " +
                                        "Reserved: {}, Grace deadline: {}, Current: {}",
                                reservation.getId(),
                                reservation.getReservedFrom().format(DATETIME_FORMATTER),
                                graceDeadline.format(DATETIME_FORMATTER),
                                now.format(DATETIME_FORMATTER));

                        // Optional: Send no-show notification
                        sendNoShowNotification(reservation);
                    }
                } catch (Exception e) {
                    log.error("❌ [SCHEDULER] Task 2: Error processing reservation {}",
                            reservation.getId(), e);
                }
            }

            log.info("✅ [SCHEDULER] Task 2: Completed. Marked {} reservations as EXPIRED (no-show)", noShowCount);
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Task 2: Fatal error in detectNoShowReservations", e);
        }
    }

    /**
     * Task 3: Send subscription expiration reminder 7 days before endDate
     * Runs daily at 9:00 AM to check for expiring subscriptions
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Ho_Chi_Minh") // Daily at 9:00 AM
    @Transactional(readOnly = true)
    public void sendSubscriptionExpirationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderStart = now.plusDays(6); // 6 days from now
        LocalDateTime reminderEnd = now.plusDays(8);   // 8 days from now

        log.info("📆 [SCHEDULER] Task 3: Checking for subscriptions expiring between {} and {}",
                reminderStart.format(DATETIME_FORMATTER),
                reminderEnd.format(DATETIME_FORMATTER));

        try {
            List<UserSubscriptionStatus> statuses = Arrays.asList(
                    UserSubscriptionStatus.ACTIVE,
                    UserSubscriptionStatus.INACTIVE
            );

            List<UserSubscription> expiringSubscriptions = userSubscriptionRepository
                    .findByStatusInAndEndDateBetween(statuses, reminderStart, reminderEnd);

            log.info("📆 [SCHEDULER] Task 3: Found {} expiring subscriptions", expiringSubscriptions.size());

            for (UserSubscription subscription : expiringSubscriptions) {
                try {
                    sendSubscriptionExpirationReminder(subscription);
                } catch (Exception e) {
                    log.error("❌ [SCHEDULER] Task 3: Error sending reminder for subscription {}",
                            subscription.getId(), e);
                }
            }

            log.info("✅ [SCHEDULER] Task 3: Completed processing {} subscriptions", expiringSubscriptions.size());
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Task 3: Fatal error in sendSubscriptionExpirationReminders", e);
        }
    }

    /**
     * Task 4: Process auto-renewal for subscriptions
     * Runs every 6 hours to check for subscriptions that need renewal
     */
    @Scheduled(cron = "0 0 */6 * * ?", zone = "Asia/Ho_Chi_Minh") // Every 6 hours
    @Transactional
    public void processSubscriptionAutoRenewal() {
        LocalDateTime now = LocalDateTime.now();

        log.info("🔄 [SCHEDULER] Task 4: Starting auto-renewal processing at {}", now.format(DATETIME_FORMATTER));

        try {
            // Find subscriptions that are expired or expiring and have autoRenew enabled
            List<UserSubscriptionStatus> statuses = Arrays.asList(
                    UserSubscriptionStatus.ACTIVE,
                    UserSubscriptionStatus.INACTIVE
            );

            List<UserSubscription> subscriptionsToRenew = userSubscriptionRepository
                    .findByStatusInAndAutoRenewAndEndDateBefore(statuses, true, now);

            log.info("🔄 [SCHEDULER] Task 4: Found {} subscriptions to auto-renew", subscriptionsToRenew.size());

            int renewedCount = 0;
            int failedCount = 0;

            for (UserSubscription subscription : subscriptionsToRenew) {
                try {
                    boolean renewed = processAutoRenewal(subscription);
                    if (renewed) {
                        renewedCount++;
                    } else {
                        failedCount++;
                    }
                } catch (Exception e) {
                    log.error("❌ [SCHEDULER] Task 4: Error processing renewal for subscription {}",
                            subscription.getId(), e);
                    failedCount++;
                }
            }

            log.info("✅ [SCHEDULER] Task 4: Completed. Renewed: {}, Failed: {}", renewedCount, failedCount);
        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Task 4: Fatal error in processSubscriptionAutoRenewal", e);
        }
    }

    // ========== Helper Methods ==========

    private void sendReservationReminder(Reservation reservation) {
        log.info("🔔 [REMINDER] Sending upcoming reservation reminder for reservation {}", reservation.getId());

        User user = reservation.getUser();
        List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

        if (deviceTokens.isEmpty()) {
            log.warn("⚠️ [REMINDER] No device tokens found for user {}", user.getId());
            return;
        }

        String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, reservation.getParkingLotId());
        String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";
        String time = reservation.getReservedFrom().format(DATETIME_FORMATTER);

        String message = String.format(
                "Nhắc nhở: Bạn có lượt đặt chỗ cho xe biển số %s tại %s vào %s (còn khoảng 1 giờ nữa). Vui lòng chuẩn bị khởi hành!",
                reservation.getVehicle().getLicensePlate(),
                lotName,
                time
        );

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("parkingLotId", reservation.getParkingLotId());
        data.put("reservedFrom", reservation.getReservedFrom().toString());
        data.put("deepLink", "parkmate://reservation/" + reservation.getId());

        sendNotification(
                user,
                deviceTokens,
                NotificationEventType.RESERVATION_REMINDER,
                "NHẮC NHỞ ĐẶT CHỖ",
                message,
                data
        );
    }

    private void sendNoShowNotification(Reservation reservation) {
        log.info("🔔 [NO-SHOW] Sending no-show notification for reservation {}", reservation.getId());

        User user = reservation.getUser();
        List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

        if (deviceTokens.isEmpty()) {
            log.warn("⚠️ [NO-SHOW] No device tokens found for user {}", user.getId());
            return;
        }

        String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, reservation.getParkingLotId());
        String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";
        String time = reservation.getReservedFrom().format(DATETIME_FORMATTER);

        String message = String.format(
                "Lượt đặt chỗ của bạn tại %s vào %s đã hết hạn do bạn không đến. Số tiền đặt cọc sẽ không được hoàn trả.",
                lotName,
                time
        );

        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("parkingLotId", reservation.getParkingLotId());
        data.put("status", "EXPIRED");

        sendNotification(
                user,
                deviceTokens,
                NotificationEventType.RESERVATION_NO_SHOW,
                "ĐẶT CHỖ ĐÃ HẾT HẠN",
                message,
                data
        );
    }

    private void sendSubscriptionExpirationReminder(UserSubscription subscription) {
        log.info("🔔 [SUBSCRIPTION] Sending expiration reminder for subscription {}", subscription.getId());

        User user = subscription.getUser();
        List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

        if (deviceTokens.isEmpty()) {
            log.warn("⚠️ [SUBSCRIPTION] No device tokens found for user {}", user.getId());
            return;
        }

        String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, subscription.getParkingLotId());
        String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";
        String endDate = subscription.getEndDate().format(DATETIME_FORMATTER);

        String autoRenewStatus = subscription.getAutoRenew() ? "Tự động gia hạn: BẬT" : "Tự động gia hạn: TẮT";

        String message = String.format(
                "Gói đăng ký của bạn tại %s sẽ hết hạn vào %s. %s. Bạn có muốn tiếp tục gia hạn không?",
                lotName,
                endDate,
                autoRenewStatus
        );

        Map<String, Object> data = new HashMap<>();
        data.put("subscriptionId", subscription.getId());
        data.put("parkingLotId", subscription.getParkingLotId());
        data.put("endDate", subscription.getEndDate().toString());
        data.put("autoRenew", subscription.getAutoRenew());
        data.put("deepLink", "parkmate://subscription/" + subscription.getId() + "/renewal-decision");
        data.put("actionRequired", true);

        sendNotification(
                user,
                deviceTokens,
                NotificationEventType.SUBSCRIPTION_EXPIRING,
                "GÓI ĐĂNG KÝ SẮP HẾT HẠN",
                message,
                data
        );
    }

    private boolean processAutoRenewal(UserSubscription subscription) {
        log.info("🔄 [AUTO-RENEWAL] Processing auto-renewal for subscription {}", subscription.getId());

        try {
            // Fetch current subscription package details
            var subscriptionResponse = parkingLotClient.getSubscription(subscription.getSubscriptionPackageId());

            if (subscriptionResponse == null || subscriptionResponse.data() == null) {
                log.error("❌ [AUTO-RENEWAL] Failed to fetch subscription package details for subscription {}",
                        subscription.getId());
                handleRenewalFailure(subscription, "Không thể lấy thông tin gói đăng ký");
                return false;
            }

            var subscriptionPackage = subscriptionResponse.data();

            // TODO: Extract price from subscription package
            // For now, use the original paidAmount as a fallback
            var renewalAmount = subscription.getPaidAmount();

            // Check wallet balance
            var walletResponse = paymentClient.getWallet(subscription.getUser().getId());

            if (walletResponse == null || walletResponse.getBody() == null ||
                    walletResponse.getBody().data() == null) {
                log.error("❌ [AUTO-RENEWAL] Failed to fetch wallet for user {}", subscription.getUser().getId());
                handleRenewalFailure(subscription, "Không thể kiểm tra số dư ví");
                return false;
            }

            var wallet = walletResponse.getBody().data();
            // TODO: Compare wallet balance with renewalAmount
            // For now, assuming sufficient balance

            // TODO: Process payment via PaymentClient
            // For now, just extend the subscription

            // Extend subscription
            LocalDateTime newEndDate = subscription.getEndDate().plusMonths(1); // Assuming monthly subscription
            subscription.setEndDate(newEndDate);
            subscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(subscription);

            log.info("✅ [AUTO-RENEWAL] Successfully renewed subscription {}. New end date: {}",
                    subscription.getId(), newEndDate.format(DATETIME_FORMATTER));

            // Send success notification
            sendRenewalSuccessNotification(subscription);
            return true;

        } catch (Exception e) {
            log.error("❌ [AUTO-RENEWAL] Error processing auto-renewal for subscription {}",
                    subscription.getId(), e);
            handleRenewalFailure(subscription, "Lỗi hệ thống khi gia hạn");
            return false;
        }
    }

    private void handleRenewalFailure(UserSubscription subscription, String reason) {
        log.warn("⚠️ [AUTO-RENEWAL] Handling renewal failure for subscription {}: {}",
                subscription.getId(), reason);

        subscription.setStatus(UserSubscriptionStatus.EXPIRED);
        subscription.setAutoRenew(false);
        subscription.setUpdatedAt(LocalDateTime.now());
        userSubscriptionRepository.save(subscription);

        sendRenewalFailureNotification(subscription, reason);
    }

    private void sendRenewalSuccessNotification(UserSubscription subscription) {
        log.info("🔔 [RENEWAL] Sending renewal success notification for subscription {}", subscription.getId());

        User user = subscription.getUser();
        List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

        if (deviceTokens.isEmpty()) {
            return;
        }

        String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, subscription.getParkingLotId());
        String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";
        String newEndDate = subscription.getEndDate().format(DATETIME_FORMATTER);

        String message = String.format(
                "Gói đăng ký của bạn tại %s đã được gia hạn thành công. Ngày hết hạn mới: %s",
                lotName,
                newEndDate
        );

        Map<String, Object> data = new HashMap<>();
        data.put("subscriptionId", subscription.getId());
        data.put("parkingLotId", subscription.getParkingLotId());
        data.put("endDate", subscription.getEndDate().toString());

        sendNotification(
                user,
                deviceTokens,
                NotificationEventType.SUBSCRIPTION_RENEWED,
                "GIA HẠN THÀNH CÔNG",
                message,
                data
        );
    }

    private void sendRenewalFailureNotification(UserSubscription subscription, String reason) {
        log.info("🔔 [RENEWAL] Sending renewal failure notification for subscription {}", subscription.getId());

        User user = subscription.getUser();
        List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

        if (deviceTokens.isEmpty()) {
            return;
        }

        String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, subscription.getParkingLotId());
        String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";

        String message = String.format(
                "Gia hạn gói đăng ký tại %s thất bại. Lý do: %s. Vui lòng nạp tiền và gia hạn thủ công.",
                lotName,
                reason
        );

        Map<String, Object> data = new HashMap<>();
        data.put("subscriptionId", subscription.getId());
        data.put("parkingLotId", subscription.getParkingLotId());
        data.put("reason", reason);
        data.put("deepLink", "parkmate://subscription/" + subscription.getId());

        sendNotification(
                user,
                deviceTokens,
                NotificationEventType.SUBSCRIPTION_RENEWAL_FAILED,
                "GIA HẠN THẤT BẠI",
                message,
                data
        );
    }

    private void sendNotification(
            User user,
            List<String> deviceTokens,
            NotificationEventType eventType,
            String title,
            String message,
            Map<String, Object> data
    ) {
        try {
            String dataJson = null;
            if (data != null && !data.isEmpty()) {
                dataJson = objectMapper.writeValueAsString(data);
            }

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType.getValue())
                    .recipientId(user.getAccount().getId())
                    .recipientEmail(user.getAccount().getEmail())
                    .title(title)
                    .message(message)
                    .notificationType("PUSH")
                    .deviceTokens(deviceTokens)
                    .data(dataJson)
                    .createdAt(LocalDateTime.now())
                    .sourceService("user-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event
            );

            log.info("✅ [NOTIFICATION] Published {} notification to {} devices",
                    eventType.getValue(), deviceTokens.size());
        } catch (Exception e) {
            log.error("❌ [NOTIFICATION] Failed to send notification", e);
        }
    }
}

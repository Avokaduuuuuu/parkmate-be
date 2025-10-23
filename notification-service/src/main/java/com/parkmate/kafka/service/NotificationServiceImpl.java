package com.parkmate.kafka.service;

import com.parkmate.email.EmailService;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.event.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Override
    public void sendNotification(NotificationEvent notificationEvent) {
        try {
            log.info("Processing notification for event: {}", notificationEvent.getEventId());

            if (isEmailNotification(notificationEvent)) {
                sendEmailNotification(notificationEvent);
            }

            if (isPushNotification(notificationEvent)) {
                sendPushNotification(notificationEvent);
            }
            logNotificationStatus(notificationEvent, true, "Notification sent successfully");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            logNotificationStatus(notificationEvent, false, e.getMessage());
            throw e;
        }
    }

    @Override
    public void sendEmailNotification(NotificationEvent notificationEvent) {
        try {
            String recipientEmail = getRecipientEmail(notificationEvent);
            String recipientName = notificationEvent.getTitle();
            String token = notificationEvent.getData();

            switch (NotificationEventType.fromString(notificationEvent.getEventType())) {
                case MEMBER_VERIFICATION ->
                        emailService.sendMemberVerificationEmail(recipientEmail, token, recipientName);
                case PARTNER_VERIFICATION ->
                        emailService.sendPartnerVerificationEmail(recipientEmail, token, recipientName);
                default -> log.warn("Unknown event type: {}", notificationEvent.getEventType());
            }

            log.info("Verification email sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendPushNotification(NotificationEvent notificationEvent) {
    }

    private String getRecipientEmail(NotificationEvent event) {
        return event.getRecipientEmail();
    }

    private void logNotificationStatus(NotificationEvent event, boolean success, String message) {
        if (success) {
            log.info("Notification {} - Event: {} - Message: {}",
                    "SUCCESS", event.getEventId(), message);
        } else {
            log.error("Notification {} - Event: {} - Message: {}",
                    "FAILED", event.getEventId(), message);
        }
    }

    private String getEmailSubject(String eventType) {
        return switch (eventType) {
            case "RESERVATION_CREATED" -> "Reservation Confirmed";
            case "RESERVATION_CANCELLED" -> "Reservation Cancelled";
            case "RESERVATION_COMPLETED" -> "Reservation Receipt";
            default -> "ParkMate Notification";
        };
    }

    private boolean isEmailNotification(NotificationEvent event) {
        return "EMAIL".equalsIgnoreCase(event.getNotificationType());
    }

    private boolean isPushNotification(NotificationEvent event) {
        return "PUSH".equalsIgnoreCase(event.getNotificationType());
    }

}

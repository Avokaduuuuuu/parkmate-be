package com.parkmate.kafka.service;

import com.google.firebase.messaging.BatchResponse;
import com.parkmate.email.EmailService;
import com.parkmate.fcm.FCMService;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.event.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final FCMService fcmService;

    @Override
    public void sendNotification(NotificationEvent notificationEvent) {
        try {
            log.info("🔔 [NOTIFICATION-SERVICE] Processing notification for event: {}, type: {}",
                    notificationEvent.getEventId(), notificationEvent.getNotificationType());

            if (isEmailNotification(notificationEvent)) {
                log.info("📧 [NOTIFICATION-SERVICE] Sending EMAIL notification for event: {}", notificationEvent.getEventId());
                sendEmailNotification(notificationEvent);
            }

            if (isPushNotification(notificationEvent)) {
                log.info("📱 [NOTIFICATION-SERVICE] Sending PUSH notification for event: {}", notificationEvent.getEventId());
                sendPushNotification(notificationEvent);
            }
            logNotificationStatus(notificationEvent, true, "Notification sent successfully");

        } catch (Exception e) {
            log.error("❌ [NOTIFICATION-SERVICE] Error processing notification: {}", e.getMessage(), e);
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
                case PASSWORD_RESET ->
                        emailService.sendPasswordResetEmail(recipientEmail, token, recipientName);
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
        try {
            List<String> deviceTokens = notificationEvent.getDeviceTokens();

            if (deviceTokens == null || deviceTokens.isEmpty()) {
                log.warn("⚠️ [FCM] No device tokens found for event: {}", notificationEvent.getEventId());
                return;
            }

            log.info("📱 [FCM] Sending push notification to {} device tokens", deviceTokens.size());
            log.info("📱 [FCM] Title: {}, Body: {}", notificationEvent.getTitle(), notificationEvent.getMessage());

            String title = notificationEvent.getTitle();
            String body = notificationEvent.getMessage();

            Map<String, String> data = buildDataPayload(notificationEvent);
            log.info("📱 [FCM] Data payload: {}", data);

            if (deviceTokens.size() == 1) {
                log.info("📱 [FCM] Sending to single device: {}", deviceTokens.get(0));
                String messageId = fcmService.sendToDevice(deviceTokens.get(0), title, body, data);
                log.info("📱 [FCM] Message ID: {}", messageId);

                if (messageId != null) {
                    log.info("✅ [FCM] Push notification sent successfully to: {}", deviceTokens.get(0));
                } else {
                    log.error("❌ [FCM] Failed to send push notification to: {}", deviceTokens.get(0));
                }

            } else {
                log.info("📱 [FCM] Sending to multiple devices: {}", deviceTokens.size());
                BatchResponse response = fcmService.sendToMultipleDevices(deviceTokens, title, body, data);

                if (response != null) {
                    log.info("✅ [FCM] Push notification sent to {} device tokens. Success: {}, Failure: {}",
                            deviceTokens.size(), response.getSuccessCount(), response.getFailureCount());
                } else {
                    log.error("❌ [FCM] Failed to send push notification to {} device tokens", deviceTokens.size());
                }
            }
        } catch (Exception e) {
            log.error("❌ [FCM] Error sending push notification for event: {}",
                    notificationEvent.getEventId(), e);
        }


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
        String type = event.getNotificationType();
        return "EMAIL".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type);
    }

    private boolean isPushNotification(NotificationEvent event) {
        String type = event.getNotificationType();
        return "PUSH".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type);
    }

    private Map<String, String> buildDataPayload(NotificationEvent event) {
        Map<String, String> data = new HashMap<>();

        data.put("eventId", event.getEventId());
        data.put("eventType", event.getEventType());
        if (event.getSourceService() != null) {
            data.put("sourceService", event.getSourceService());
        }

        if (event.getCreatedAt() != null) {
            data.put("createdAt", event.getCreatedAt().toString());
        }

        if (event.getData() != null && !event.getData().isEmpty()) {
            data.put("customData", event.getData());
        }

        if (event.getRecipientId() != null) {
            data.put("recipientId", event.getRecipientId().toString());
        }

        // Deep link for mobile app navigation
        String deepLink = getDeepLinkForEventType(event.getEventType(), event.getData());
        data.put("deepLink", deepLink);
        data.put("clickAction", "FLUTTER_NOTIFICATION_CLICK");

        return data;
    }

    private String getDeepLinkForEventType(String eventType, String eventData) {
        return switch (eventType) {
            case "RESERVATION_CREATED", "RESERVATION_UPDATED", "RESERVATION_ACTIVATED",
                 "RESERVATION_COMPLETED", "RESERVATION_CANCELLED" -> "parkmate://reservation/" + extractId(eventData);

            case "VEHICLE_ENTERED", "VEHICLE_EXITED" -> "parkmate://parking-session/" + extractId(eventData);

            case "PARKING_REMINDER" -> "parkmate://reservations";

            case "PARTNER_APPROVED", "PARTNER_REJECTED" -> "parkmate://partner/status";

            default -> "parkmate://home";
        };
    }

    private String extractId(String eventData) {
        if (eventData == null || eventData.isEmpty()) {
            return "";
        }

        try {
            // Simple extraction for format: {"key":"value"}
            String[] parts = eventData.split(":");
            if (parts.length >= 2) {
                return parts[1].replaceAll("[\"{}]", "").trim();
            }
        } catch (Exception e) {
            log.debug("Could not extract ID from data: {}", eventData);
        }

        return "";
    }
}

package com.parkmate.account.publisher;


import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountEventPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    // Method gửi email verification cho Member
    public void publishMemberVerificationEvent(String email, String name, String verificationCode) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("MEMBER_VERIFICATION")
                    .recipientId(null)
                    .recipientEmail(email)
                    .title(name)
                    .message("Please verify your email")
                    .notificationType("EMAIL")
                    .data(verificationCode)
                    .createdAt(LocalDateTime.now())
                    .sourceService("user-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event);
            log.info("Member verification event published for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to publish member verification event", e);
        }
    }

    public void publishPartnerVerificationEvent(String email, String contactPersonName, String verificationCode) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PARTNER_VERIFICATION")
                    .recipientId(null)
                    .recipientEmail(email)
                    .title(contactPersonName)
                    .message("Please verify your partner email")
                    .notificationType("EMAIL")
                    .data(verificationCode)
                    .createdAt(LocalDateTime.now())
                    .sourceService("user-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event
            );

            log.info("Partner verification event published for email: {}", email);

        } catch (Exception e) {
            log.error("Failed to publish partner verification event", e);
        }
    }

    public void publishPasswordResetEvent(String email, String name, String resetCode) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PASSWORD_RESET")
                    .recipientId(null)
                    .recipientEmail(email)
                    .title(name)
                    .message("Password reset code")
                    .notificationType("EMAIL")
                    .data(resetCode)
                    .createdAt(LocalDateTime.now())
                    .sourceService("user-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event);
            log.info("Password reset event published for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to publish password reset event", e);
        }
    }
}

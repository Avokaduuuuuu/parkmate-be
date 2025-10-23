package com.parkmate.kafka.listener;

import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenNotificationEvent(
            @Payload NotificationEvent notificationEvent,
            Acknowledgment acknowledgment) {
        try {
            log.info("Received notification event: {}", notificationEvent.getEventId());

            // Acknowledge immediately - async processing will handle sending
            acknowledgment.acknowledge();

            // Process async to avoid blocking Kafka consumer
            sendNotificationAsync(notificationEvent);

            log.info("Notification event queued for sending: {}", notificationEvent.getEventId());
        } catch (Exception e) {
            log.error("Error occurred while processing notification event: {}", notificationEvent.getEventId(), e);
        }

    }

    @Async("emailExecutor")
    public void sendNotificationAsync(NotificationEvent notificationEvent) {
        try {
            notificationService.sendNotification(notificationEvent);
            log.info("Notification sent successfully: {}", notificationEvent.getEventId());
        } catch (Exception e) {
            log.error("Error sending notification: {}", notificationEvent.getEventId(), e);
        }
    }

}

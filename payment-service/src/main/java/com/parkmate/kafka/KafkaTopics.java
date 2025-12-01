package com.parkmate.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopics {
    NOTIFICATION("notification-events", "Notification events for emails and push notifications");

    private final String topicName;
    private final String description;

    KafkaTopics(String topicName, String description) {
        this.topicName = topicName;
        this.description = description;
    }
}

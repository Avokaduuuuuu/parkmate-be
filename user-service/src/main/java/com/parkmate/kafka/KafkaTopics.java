package com.parkmate.kafka;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopics {

    NOTIFICATION("notification-events"),
    EMAIL("email-events"),
    PUSH_NOTIFICATION("push-notification-events"),
    RESERVATION("reservation-events");

    private final String topicName;

}

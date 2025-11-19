package com.parkmate.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    String eventId;
    String eventType;
    Long recipientId;
    String recipientEmail;
    String title;
    String message;
    String notificationType;
    List<String> deviceTokens;
    String data;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    String sourceService;
}

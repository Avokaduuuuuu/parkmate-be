package com.parkmate.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Cho phép CORS từ HTML file
public class FCMTestController {

    private final FCMService fcmService;

    /**
     * Subscribe device token to topic
     */
    @PostMapping("/subscribe-topic")
    public Map<String, Object> subscribeToTopic(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String topic = request.get("topic");

        if (token == null || topic == null) {
            return Map.of(
                    "success", false,
                    "error", "Token and topic are required"
            );
        }

        log.info("📱 Subscribing token to topic: {}", topic);

        try {
            // Dùng Firebase Admin SDK để subscribe
            FirebaseMessaging.getInstance().subscribeToTopic(
                    Collections.singletonList(token),
                    topic
            );

            log.info("✅ Successfully subscribed to topic: {}", topic);
            return Map.of(
                    "success", true,
                    "topic", topic,
                    "message", "Subscribed successfully"
            );

        } catch (FirebaseMessagingException e) {
            log.error("❌ Error subscribing to topic: ", e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Test endpoint để gửi notification đến topic
     */
    @PostMapping("/test/send-to-topic")
    public Map<String, Object> sendTestToTopic(
            @RequestParam String topic,
            @RequestParam String title,
            @RequestParam String body) {

        log.info("📤 Sending test notification to topic: {}", topic);

        try {
            String messageId = fcmService.sendToTopic(
                    topic,
                    title,
                    body,
                    Map.of(
                            "test", "true",
                            "source", "test-controller",
                            "timestamp", String.valueOf(System.currentTimeMillis())
                    )
            );

            if (messageId != null) {
                log.info("✅ Notification sent successfully! MessageId: {}", messageId);
                return Map.of(
                        "success", true,
                        "messageId", messageId,
                        "topic", topic
                );
            } else {
                log.error("❌ Failed to send notification - messageId is null");
                return Map.of(
                        "success", false,
                        "error", "MessageId is null"
                );
            }
        } catch (Exception e) {
            log.error("❌ Error sending notification: ", e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }
}
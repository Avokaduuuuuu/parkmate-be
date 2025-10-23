package com.parkmate.fcm;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FCMServiceImpl implements FCMService {

    @Override
    public String sendToDevice(String token, String title, String body, Map<String, String> data) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData((data != null ? data : Map.of()))
                .build();
        try {
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("Message ID: {}", messageId);
            return messageId;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public BatchResponse sendToMultipleDevices(List<String> tokens, String title, String body, Map<String, String> data) {

        if (tokens == null || tokens.isEmpty()) {
            log.error("No tokens provided to send FCM message");
            return null;
        }

        try {
            // Build notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // ⭐ CHECK LIMIT TRƯỚC KHI BUILD MESSAGE
            if (tokens.size() > 500) {
                log.warn("Token count {} exceeds FCM limit (500), processing in batches", tokens.size());
                return sendInBatches(tokens, notification, data);
            }

            // Single batch (≤ 500 tokens)
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(notification)
                    .putAllData(data != null ? data : Map.of())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("✅ Successfully sent message to {} devices (success: {}, failed: {})",
                    tokens.size(),
                    response.getSuccessCount(),
                    response.getFailureCount());

            // Log failed tokens
            if (response.getFailureCount() > 0) {
                logFailedTokens(tokens, response.getResponses());
            }

            return response;

        } catch (Exception e) {
            log.error("❌ Failed to send FCM message: {}", e.getMessage(), e);
            return null;
        }
    }


    private BatchResponse sendInBatches(List<String> tokens, Notification notification, Map<String, String> data) {
        int batchSize = 500;
        int totalSuccess = 0;
        int totalFailure = 0;
        List<SendResponse> allResponses = new java.util.ArrayList<>();

        try {
            for (int i = 0; i < tokens.size(); i += batchSize) {
                List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));

                log.info("📦 Processing batch {}/{} ({} tokens)",
                        (i / batchSize) + 1,
                        (tokens.size() + batchSize - 1) / batchSize,
                        batch.size());

                // Build message for this batch
                MulticastMessage message = MulticastMessage.builder()
                        .addAllTokens(batch)
                        .setNotification(notification)
                        .putAllData(data != null ? data : Map.of())
                        .build();

                // Send batch
                BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);

                totalSuccess += batchResponse.getSuccessCount();
                totalFailure += batchResponse.getFailureCount();
                allResponses.addAll(batchResponse.getResponses());

                log.info("✅ Batch sent: {} success, {} failed",
                        batchResponse.getSuccessCount(),
                        batchResponse.getFailureCount());

                // Log failed tokens in this batch
                if (batchResponse.getFailureCount() > 0) {
                    logFailedTokens(batch, batchResponse.getResponses());
                }
            }

            log.info("🎯 All batches completed: Total {} success, {} failed", totalSuccess, totalFailure);

            // Create combined BatchResponse
            return createBatchResponse(totalSuccess, totalFailure, allResponses);

        } catch (Exception e) {
            log.error("❌ Failed to send batches: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Log failed tokens with error messages
     */
    private void logFailedTokens(List<String> tokens, List<SendResponse> responses) {
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                String token = tokens.get(i);
                Exception exception = responses.get(i).getException();

                log.error("❌ Failed token[{}]: {}, error: {}",
                        i,
                        maskToken(token),  // Mask token for security
                        exception != null ? exception.getMessage() : "Unknown error");
            }
        }
    }

    /**
     * Mask FCM token for logging (security)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 4);
    }

    /**
     * Create BatchResponse from aggregated results
     * Note: This is a workaround since BatchResponse doesn't have public constructor
     */
    private BatchResponse createBatchResponse(int successCount, int failureCount, List<SendResponse> responses) {
        // BatchResponse doesn't have public constructor, so we return the last batch response
        // For production, consider creating a custom wrapper class

        // Workaround: Return a summary via logging
        log.info("📊 Aggregated results: {} total success, {} total failures", successCount, failureCount);

        // Return null and rely on logs, or create custom response wrapper
        return null;  // Consider creating CustomBatchResponse class
    }


    @Override
    public String sendToTopic(String topic, String title, String body, Map<String, String> data) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .putAllData((data != null ? data : Map.of()))
                .build();
        try {
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("Message ID: {}", messageId);
            return messageId;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message: {}", e.getMessage(), e);
            return null;
        }
    }
}

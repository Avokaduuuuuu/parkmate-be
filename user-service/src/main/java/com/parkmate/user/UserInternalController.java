package com.parkmate.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.dto.response.WalletResponse;
import com.parkmate.common.dto.ApiResponse;
import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.event.NotificationEventType;
import com.parkmate.mobileDevice.MobileDeviceRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class UserInternalController {

    private final UserRepository userRepository;
    private final PaymentClient paymentClient;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, LocalDateTime> notificationCache = new HashMap<>();
    private static final int NOTIFICATION_COOLDOWN_MINUTES = 30;

    @PostMapping("/{userId}/check-wallet-balance")
    public ApiResponse<Boolean> checkWalletBalance(
            @PathVariable Long userId,
            @RequestParam String sessionId,
            @RequestParam BigDecimal requiredAmount
    ) {
        log.info("💰 [WALLET-CHECK] Checking wallet balance for user {}, session {}, required: {}",
                userId, sessionId, requiredAmount);

        try {
            var walletResponse = paymentClient.getWallet(userId);

            if (walletResponse == null || walletResponse.getBody() == null ||
                walletResponse.getBody().data() == null) {
                log.error("❌ [WALLET-CHECK] Failed to fetch wallet for user {}", userId);
                return ApiResponse.success(false);
            }

            WalletResponse wallet = walletResponse.getBody().data();
            BigDecimal balance = BigDecimal.valueOf(wallet.balance());

            log.info("💰 [WALLET-CHECK] User {} wallet balance: {}, required: {}",
                    userId, balance, requiredAmount);

            if (balance.compareTo(requiredAmount) < 0) {
                log.warn("⚠️ [WALLET-CHECK] Insufficient balance for user {}. Balance: {}, Required: {}",
                        userId, balance, requiredAmount);

                String cacheKey = userId + ":" + sessionId;
                LocalDateTime lastNotification = notificationCache.get(cacheKey);
                LocalDateTime now = LocalDateTime.now();

                if (lastNotification == null) {
                    sendLowBalanceNotification(userId, sessionId, balance, requiredAmount);
                    notificationCache.put(cacheKey, now);
                } else if (lastNotification.plusMinutes(NOTIFICATION_COOLDOWN_MINUTES).isBefore(now)) {
                    sendLowBalanceReminder(userId, sessionId, balance, requiredAmount);
                    notificationCache.put(cacheKey, now);
                } else {
                    log.info("ℹ️ [WALLET-CHECK] Notification cooldown active for user {}, session {}. Last sent: {}",
                            userId, sessionId, lastNotification);
                }

                return ApiResponse.success(false);
            }

            notificationCache.remove(userId + ":" + sessionId);
            log.info("✅ [WALLET-CHECK] Sufficient balance for user {}", userId);
            return ApiResponse.success(true);

        } catch (Exception e) {
            log.error("❌ [WALLET-CHECK] Error checking wallet balance for user {}", userId, e);
            return ApiResponse.success(false);
        }
    }

    private void sendLowBalanceNotification(Long userId, String sessionId, BigDecimal balance, BigDecimal required) {
        log.info("🔔 [LOW-BALANCE] Sending low balance notification to user {}", userId);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.error("❌ [LOW-BALANCE] User {} not found", userId);
                return;
            }

            List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(userId);
            if (deviceTokens.isEmpty()) {
                log.warn("⚠️ [LOW-BALANCE] No device tokens found for user {}", userId);
                return;
            }

            String message = String.format(
                    "Số dư ví của bạn (₫%,d) không đủ để thanh toán phí đỗ xe hiện tại (₫%,d). Vui lòng nạp thêm ngay để tránh gián đoạn dịch vụ.",
                    balance.longValue(),
                    required.longValue()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("currentBalance", balance.toString());
            data.put("requiredAmount", required.toString());
            data.put("deficit", required.subtract(balance).toString());
            data.put("deepLink", "parkmate://wallet/top-up");

            String dataJson = objectMapper.writeValueAsString(data);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(NotificationEventType.LOW_WALLET_BALANCE.getValue())
                    .recipientId(user.getAccount().getId())
                    .recipientEmail(user.getAccount().getEmail())
                    .title("SỐ DƯ VÍ KHÔNG ĐỦ")
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

            log.info("✅ [LOW-BALANCE] Notification sent to user {} ({} devices)", userId, deviceTokens.size());
        } catch (Exception e) {
            log.error("❌ [LOW-BALANCE] Error sending notification to user {}", userId, e);
        }
    }

    private void sendLowBalanceReminder(Long userId, String sessionId, BigDecimal balance, BigDecimal required) {
        log.info("🔔 [LOW-BALANCE] Sending low balance REMINDER to user {}", userId);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return;
            }

            List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(userId);
            if (deviceTokens.isEmpty()) {
                return;
            }

            String message = String.format(
                    "Nhắc nhở: Số dư ví của bạn (₫%,d) vẫn chưa đủ để thanh toán phí đỗ xe (₫%,d). Vui lòng nạp tiền ngay để hoàn tất thanh toán.",
                    balance.longValue(),
                    required.longValue()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("currentBalance", balance.toString());
            data.put("requiredAmount", required.toString());
            data.put("deficit", required.subtract(balance).toString());
            data.put("deepLink", "parkmate://wallet/top-up");
            data.put("isReminder", true);

            String dataJson = objectMapper.writeValueAsString(data);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(NotificationEventType.LOW_WALLET_BALANCE.getValue())
                    .recipientId(user.getAccount().getId())
                    .recipientEmail(user.getAccount().getEmail())
                    .title("NHẮC NHỞ: SỐ DƯ VÍ KHÔNG ĐỦ")
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

            log.info("✅ [LOW-BALANCE] Reminder sent to user {} ({} devices)", userId, deviceTokens.size());
        } catch (Exception e) {
            log.error("❌ [LOW-BALANCE] Error sending reminder to user {}", userId, e);
        }
    }
}

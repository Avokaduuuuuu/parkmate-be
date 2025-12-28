package com.parkmate.kafka.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OperationalPaymentNotificationPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void publishPaymentReminder(Long partnerId, String partnerEmail, String parkingLotName,
                                        BigDecimal amount, LocalDateTime dueDate, String paymentUrl) {
        try {
            String dueDateStr = dueDate.format(FORMATTER);
            String amountStr = String.format("%,d", amount.longValue());

            String message = String.format(
                    "Kính gửi Đối tác,\n\n" +
                    "Đây là thông báo nhắc nhở về khoản phí vận hành cho bãi xe \"%s\".\n\n" +
                    "Thông tin thanh toán:\n" +
                    "- Số tiền: %s VNĐ\n" +
                    "- Hạn thanh toán: %s\n\n" +
                    "Vui lòng thực hiện thanh toán trước hạn để tránh gián đoạn dịch vụ.\n" +
                    "Link thanh toán: %s\n\n" +
                    "Trân trọng,\n" +
                    "ParkMate Team",
                    parkingLotName, amountStr, dueDateStr, paymentUrl
            );

            Map<String, Object> data = new HashMap<>();
            data.put("partnerId", partnerId);
            data.put("parkingLotName", parkingLotName);
            data.put("amount", amount);
            data.put("dueDate", dueDate.toString());
            data.put("paymentUrl", paymentUrl);

            String dataJson = objectMapper.writeValueAsString(data);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("OPERATIONAL_PAYMENT_REMINDER")
                    .recipientId(partnerId)
                    .recipientEmail(partnerEmail)
                    .title("Nhắc nhở thanh toán phí vận hành bãi xe")
                    .message(message)
                    .notificationType("EMAIL")
                    .data(dataJson)
                    .createdAt(LocalDateTime.now().plusHours(7))
                    .sourceService("payment-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event
            );

            log.info("Published operational payment reminder for partner {} to Kafka", partnerId);
        } catch (Exception e) {
            log.error("Failed to publish operational payment reminder for partner {}", partnerId, e);
        }
    }

    public void publishPaymentOverdue(Long partnerId, String partnerEmail, String parkingLotName,
                                       BigDecimal amount, LocalDateTime dueDate) {
        try {
            String dueDateStr = dueDate.format(FORMATTER);
            String amountStr = String.format("%,d", amount.longValue());

            String message = String.format(
                    "Kính gửi Đối tác,\n\n" +
                    "Khoản phí vận hành cho bãi xe \"%s\" đã QUÁ HẠN THANH TOÁN.\n\n" +
                    "Thông tin:\n" +
                    "- Số tiền: %s VNĐ\n" +
                    "- Hạn thanh toán: %s\n\n" +
                    "CẢNH BÁO: Nếu không thanh toán trong vòng 3 ngày, bãi xe của bạn sẽ bị TẠM NGƯNG HOẠT ĐỘNG.\n\n" +
                    "Vui lòng liên hệ ngay với chúng tôi để được hỗ trợ.\n\n" +
                    "Trân trọng,\n" +
                    "ParkMate Team\n" +
                    "Email: support@parkmate.vn\n" +
                    "Hotline: 1900-xxxx",
                    parkingLotName, amountStr, dueDateStr
            );

            Map<String, Object> data = new HashMap<>();
            data.put("partnerId", partnerId);
            data.put("parkingLotName", parkingLotName);
            data.put("amount", amount);
            data.put("dueDate", dueDate.toString());
            data.put("status", "OVERDUE");

            String dataJson = objectMapper.writeValueAsString(data);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("OPERATIONAL_PAYMENT_OVERDUE")
                    .recipientId(partnerId)
                    .recipientEmail(partnerEmail)
                    .title("CẢNH BÁO: Phí vận hành bãi xe đã quá hạn")
                    .message(message)
                    .notificationType("EMAIL")
                    .data(dataJson)
                    .createdAt(LocalDateTime.now().plusHours(7))
                    .sourceService("payment-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event
            );

            log.info("Published operational payment overdue notification for partner {} to Kafka", partnerId);
        } catch (Exception e) {
            log.error("Failed to publish operational payment overdue for partner {}", partnerId, e);
        }
    }
}

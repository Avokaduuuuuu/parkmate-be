package com.parkmate.walletTransaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.UserServiceClient;
import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.wallet.Wallet;
import com.parkmate.wallet.WalletOwner;
import com.parkmate.wallet.WalletRepository;
import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.SessionPaymentCreateRequest;
import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import com.querydsl.core.types.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionMapper walletTransactionMapper;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WalletTransactionResponse createWalletTransaction(CreateTransactionRequest request) {

        log.info("Creating wallet transaction for user {}: type={}, amount={}, partnerId={}",
                request.getUserId(), request.getTransactionType(), request.getAmount(), request.getPartnerId());

        Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(request.getUserId(), WalletOwner.MEMBER)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND,
                        "Member wallet not found for user: " + request.getUserId()));

        BigDecimal memberCurrentBalance = wallet.getBalance();
        BigDecimal amount = request.getAmount();

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(request.getTransactionType());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
        BigDecimal newBalance;
        switch (transactionType) {
            case DEDUCTION, SUBSCRIPTION_PAYMENT, RESERVATION_PAYMENT -> {
                if (memberCurrentBalance.compareTo(amount) < 0) {
                    log.warn("Insufficient balance for user {}. Current: {}, Required: {}",
                            request.getUserId(), memberCurrentBalance, amount);
                    throw new AppException(ErrorCode.INSUFFICIENT_WALLET_BALANCE);
                }
                newBalance = memberCurrentBalance.subtract(amount);
            }
            case TOP_UP, REFUND, REVERSAL -> {
                newBalance = memberCurrentBalance.add(amount);
                log.info("Adding to member wallet: {} + {} = {}", memberCurrentBalance, amount, newBalance);
            }
            default -> throw new AppException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        log.debug("Wallet balance updated for user {}: {} -> {}",
                request.getUserId(), memberCurrentBalance, newBalance);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(amount)
                .transactionType(transactionType)
                .status(TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now().plusHours(7))
                .build();

        walletTransaction = walletTransactionRepository.save(walletTransaction);

        WalletTransactionResponse response = walletTransactionMapper.toResponse(walletTransaction);

        response.setBalanceBefore(memberCurrentBalance);
        response.setBalanceAfter(newBalance);

        return response;
    }

    @Override
    public Page<WalletTransactionResponse> getTransactions(int page, int size, String sortBy, String sortOrder, TransactionSearchCriteria criteria, String userHeaderId, String role) {
        Long userId = null;
        Long walletId = null;

        if (userHeaderId != null) {
            try {
                userId = Long.parseLong(userHeaderId);
                log.info("Parsed user ID from header: {}", userId);

                // Get the correct wallet based on user role
                if (role != null) {
                    WalletOwner walletOwner = determineWalletOwnerFromRole(role);
                    Long finalUserId = userId;
                    Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(userId, walletOwner)
                            .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, finalUserId));
                    walletId = wallet.getId();
                    log.info("Found wallet ID {} for user {} with role {}", walletId, userId, role);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userHeaderId);
            }
        }

        // Create pageable
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        // Build predicate from criteria and walletId (not userId)
        Predicate predicate = TransactionSpecification.buildPredicate(criteria, walletId);

        // Query with predicate
        Page<WalletTransaction> transactions = walletTransactionRepository.findAll(predicate, pageable);

        // Map to response
        return transactions.map(walletTransactionMapper::toResponse);
    }

    @Override
    @Transactional
    public void createSessionCharge(SessionPaymentCreateRequest sessionPaymentCreateRequest) {
        Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(sessionPaymentCreateRequest.getUserId(), WalletOwner.MEMBER)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, sessionPaymentCreateRequest.getUserId()));

        if (wallet.getBalance().compareTo(sessionPaymentCreateRequest.getTotalFee()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_WALLET_BALANCE);
        }

        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = wallet.getBalance().subtract(sessionPaymentCreateRequest.getTotalFee());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        log.info("Updated wallet balance for user {}: {} -> {}", wallet.getHolderId(), oldBalance, newBalance);
        walletTransactionRepository.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .amount(sessionPaymentCreateRequest.getTotalFee())
                .transactionType(TransactionType.DEDUCTION)
                .status(TransactionStatus.COMPLETED)
                .description("Session charge")
                .build());

        // Send notification when vehicle exits and payment is completed
        sendSessionPaymentNotification(
                sessionPaymentCreateRequest.getUserId(),
                sessionPaymentCreateRequest.getSessionId(),
                sessionPaymentCreateRequest.getTotalFee(),
                newBalance
        );
    }

    private void sendSessionPaymentNotification(Long userId, UUID sessionId, BigDecimal amount, BigDecimal newBalance) {
        log.info("🔔 [SESSION-PAYMENT] Sending payment notification to user {} for session {}", userId, sessionId);

        try {
            var notificationInfoResponse = userServiceClient.getUserNotificationInfo(userId);
            if (notificationInfoResponse == null || notificationInfoResponse.data() == null) {
                log.warn("⚠️ [SESSION-PAYMENT] Could not fetch notification info for user {}", userId);
                return;
            }

            UserServiceClient.UserNotificationInfo userInfo = notificationInfoResponse.data();
            List<String> deviceTokens = userInfo.deviceTokens();

            if (deviceTokens == null || deviceTokens.isEmpty()) {
                log.warn("⚠️ [SESSION-PAYMENT] No device tokens found for user {}", userId);
                return;
            }

            String message = String.format(
                    "Xe của bạn đã ra khỏi bãi. Phí gửi xe: %,d VNĐ đã được trừ từ ví. Số dư hiện tại: %,d VNĐ",
                    amount.longValue(),
                    newBalance.longValue()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId.toString());
            data.put("amount", amount.toString());
            data.put("newBalance", newBalance.toString());
            data.put("deepLink", "parkmate://wallet");

            String dataJson = objectMapper.writeValueAsString(data);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("SESSION_PAYMENT")
                    .recipientId(userInfo.accountId())
                    .recipientEmail(userInfo.email())
                    .title("THANH TOÁN PHÍ GỬI XE")
                    .message(message)
                    .notificationType("PUSH")
                    .deviceTokens(deviceTokens)
                    .data(dataJson)
                    .createdAt(LocalDateTime.now().plusHours(7))
                    .sourceService("payment-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event
            );

            log.info("✅ [SESSION-PAYMENT] Notification sent to user {} ({} devices)", userId, deviceTokens.size());
        } catch (Exception e) {
            log.error("❌ [SESSION-PAYMENT] Error sending notification to user {}", userId, e);
        }
    }

    private WalletOwner determineWalletOwnerFromRole(String role) {
        if (role == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "User role is required");
        }

        // Role from gateway is typically "ROLE_PARTNER" or "ROLE_MEMBER" or "ROLE_DRIVER"
        if (role.contains("PARTNER")) {
            return WalletOwner.PARTNER;
        } else if (role.contains("MEMBER") || role.contains("DRIVER")) {
            return WalletOwner.MEMBER;
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid role: " + role);
        }
    }


}

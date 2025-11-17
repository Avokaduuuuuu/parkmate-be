package com.parkmate.payos;

import com.parkmate.walletTransaction.TransactionStatus;
import com.parkmate.walletTransaction.TransactionType;
import com.parkmate.walletTransaction.WalletTransaction;
import com.parkmate.walletTransaction.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to automatically expire/cancel pending TOP_UP transactions after 15 minutes.
 * This prevents transactions from staying in PENDING status forever if user doesn't complete payment.
 *
 * Process:
 * 1. Every 5 minutes, check all PENDING TOP_UP transactions
 * 2. For transactions older than 15 minutes, mark as FAILED (expired/timeout)
 * 3. Webhook should handle successful payments before expiration
 *
 * Note: We rely on webhook for successful payments. If webhook is missed and payment is actually PAID,
 * the user can contact support to manually verify and credit their wallet.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentChecker {

    private final WalletTransactionRepository walletTransactionRepository;

    @Value("${payment.expiration.minutes:15}")
    private int expirationMinutes;

    // Run every 5 minutes (300000 milliseconds)
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkExpiredPendingPayments() {
        try {
            log.info("Starting scheduled payment expiration check...");

            // Find all PENDING TOP_UP transactions
            List<WalletTransaction> pendingPayments = walletTransactionRepository
                    .findByTransactionTypeAndStatus(TransactionType.TOP_UP, TransactionStatus.PENDING);

            if (pendingPayments.isEmpty()) {
                log.debug("No pending TOP_UP transactions to check");
                return;
            }

            log.info("Found {} pending TOP_UP transaction(s) to check", pendingPayments.size());

            LocalDateTime now = LocalDateTime.now();
            int expiredCount = 0;

            for (WalletTransaction transaction : pendingPayments) {
                try {
                    // Calculate time elapsed since transaction creation
                    LocalDateTime createdAt = transaction.getCreatedAt();
                    long minutesElapsed = java.time.Duration.between(createdAt, now).toMinutes();

                    // Check transactions older than configured minutes (default 15)
                    if (minutesElapsed >= expirationMinutes) {
                        // Mark as FAILED (expired/timeout)
                        transaction.setStatus(TransactionStatus.FAILED);
                        transaction.setProcessedAt(now);

                        // Add metadata about expiration
                        String metadata = String.format(
                                "{\"reason\":\"auto-expired\",\"minutesElapsed\":%d,\"expiredAt\":\"%s\",\"expirationThreshold\":%d}",
                                minutesElapsed, now, expirationMinutes
                        );
                        transaction.setMetadata(metadata);

                        walletTransactionRepository.save(transaction);
                        expiredCount++;

                        log.info("✗ Expired pending payment - orderCode: {}, created: {}, elapsed: {} minutes",
                                transaction.getExternalTransactionId(), createdAt, minutesElapsed);
                    } else {
                        log.debug("Payment still within valid period - orderCode: {}, elapsed: {}/{} minutes",
                                transaction.getExternalTransactionId(), minutesElapsed, expirationMinutes);
                    }

                } catch (Exception e) {
                    log.error("Error processing transaction: {}", transaction.getId(), e);
                }
            }

            log.info("Completed scheduled payment expiration check - expired: {}/{} pending payments",
                    expiredCount, pendingPayments.size());

        } catch (Exception e) {
            log.error("Error during scheduled payment expiration check", e);
        }
    }
}

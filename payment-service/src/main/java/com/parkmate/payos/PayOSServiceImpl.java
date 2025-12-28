package com.parkmate.payos;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.common.QRCodeGenerator;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.operationalPayment.OperationalPaymentEntity;
import com.parkmate.operationalPayment.OperationalPaymentRepository;
import com.parkmate.operationalPayment.enums.PaymentStatus;
import com.parkmate.payos.dto.PaymentCancelResponse;
import com.parkmate.payos.dto.PaymentStatusResponse;
import com.parkmate.wallet.Wallet;
import com.parkmate.wallet.WalletOwner;
import com.parkmate.wallet.WalletRepository;
import com.parkmate.walletTransaction.TransactionStatus;
import com.parkmate.walletTransaction.TransactionType;
import com.parkmate.walletTransaction.WalletTransaction;
import com.parkmate.walletTransaction.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v1.payouts.Payout;
import vn.payos.model.v1.payouts.PayoutRequests;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;              // For payments (top-up)
    private final PayOS payOSPayout;        // For payouts (withdrawal)
    private final PayOSConfig payOSConfig;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final QRCodeGenerator qrCodeGenerator;
    private final OperationalPaymentRepository operationalPaymentRepository;
    private final ParkingLotClient parkingLotClient;


    private final String TOP_UP_DESCRIPTION = "TOP UP WALLET";

    @Override
    @Transactional
    public CreatePaymentLinkResponse createPayment(String userHeadId, Long amount) {
        try {


            // Validate
            if (userHeadId == null) {
                throw new IllegalArgumentException("Invalid userId");
            }
            if (amount == null || amount < 1000) {
                throw new IllegalArgumentException("Amount must be at least 1000 VND");
            }

            Long userId = Long.parseLong(userHeadId);

            Wallet wallet = walletRepository.findByHolderIdAndWalletOwner(userId, WalletOwner.MEMBER)
                    .orElseThrow(() -> new IllegalArgumentException("Member wallet not found for user: " + userId));

            long orderCode = System.currentTimeMillis();

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name("Top up wallet")
                    .quantity(1)
                    .price(amount)
                    .build();

            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(TOP_UP_DESCRIPTION)
                    .items(List.of(item))
                    .returnUrl(payOSConfig.getReturnUrl())
                    .cancelUrl(payOSConfig.getCancelUrl())
                    .build();

            // Create payment link with PayOS
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);

            // Create pending transaction
            WalletTransaction transaction = WalletTransaction.builder()
                    .walletId(wallet.getId())
                    .transactionType(TransactionType.TOP_UP)
                    .amount(BigDecimal.valueOf(amount))
                    .externalTransactionId(String.valueOf(orderCode))
                    .status(TransactionStatus.PENDING)
                    .description(TOP_UP_DESCRIPTION)
                    .build();

            walletTransactionRepository.save(transaction);

            log.info("Created PayOS payment - orderCode: {}, checkoutUrl: {}", orderCode, response.getCheckoutUrl());

            // Return the original response from PayOS SDK (contains all required fields)
            String qrBase64;
            try {
                String vietQRString = response.getQrCode();
                qrBase64 = qrCodeGenerator.generateQRCodeBase64(vietQRString);
                log.info("QR code generated successfully for order: {}",
                        response.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to generate QR code", e);
                qrBase64 = response.getQrCode();
            }
            response.setQrCode(qrBase64);
            return response;

        } catch (Exception e) {
            log.error("Error creating PayOS payment for userId: {}, amount: {}", userHeadId, amount, e);
            throw new AppException(ErrorCode.WALLET_TOPUP_FAILED, e.getMessage());
        }
    }

    @Override
    @Transactional
    public CreatePaymentLinkResponse createOperationalFeePayment(Long operationalPaymentId, Long lotId, Long amount) {
        try {
            log.info("Creating operational fee payment - paymentId: {}, lotId: {}, amount: {}",
                    operationalPaymentId, lotId, amount);

            // Validate
            if (operationalPaymentId == null) {
                throw new IllegalArgumentException("Invalid operational payment ID");
            }
            if (lotId == null) {
                throw new IllegalArgumentException("Invalid lot ID");
            }
            if (amount == null || amount < 1000) {
                throw new AppException(ErrorCode.PAYOS_INVALID_AMOUNT);
            }

            // Generate unique orderCode using timestamp and payment ID
            long orderCode = System.currentTimeMillis();

            // Create item data for PayOS
            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(String.format("Operational fee - Lot #%d", lotId))
                    .quantity(1)
                    .price(amount)
                    .build();

            // Create payment link request
            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(String.format("Lot #%d fee", lotId))
                    .items(List.of(item))
                    .returnUrl(payOSConfig.getReturnUrl())
                    .cancelUrl(payOSConfig.getCancelUrl())
                    .build();

            // Create payment link with PayOS
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);

            log.info("Created PayOS operational payment - orderCode: {}, checkoutUrl: {}",
                    orderCode, response.getCheckoutUrl());

            // Generate QR code
            String qrBase64;
            try {
                String vietQRString = response.getQrCode();
                qrBase64 = qrCodeGenerator.generateQRCodeBase64(vietQRString);
                log.info("QR code generated successfully for operational payment order: {}",
                        response.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to generate QR code for operational payment", e);
                qrBase64 = response.getQrCode();
            }
            response.setQrCode(qrBase64);

            return response;

        } catch (Exception e) {
            log.error("Error creating PayOS operational fee payment for lot: {}, amount: {}",
                    lotId, amount, e);
            throw new AppException(ErrorCode.PAYOS_PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    @Transactional
    public Boolean processWebhook(String webhookBody, String signature) {
        try {
            // Log raw webhook for debugging
            log.info("Processing PayOS webhook - signature present: {}", signature != null);
            log.debug("Raw webhook body: {}", webhookBody);

            // Verify webhook signature and parse data
            WebhookData webhookData = payOS.webhooks().verify(webhookBody);

            log.info("PayOS webhook verified - orderCode: {}, code: {}, amount: {}",
                    webhookData.getOrderCode(), webhookData.getCode(), webhookData.getAmount());

            // Find transaction by orderCode (wallet transaction)
            WalletTransaction transaction = walletTransactionRepository
                    .findByExternalTransactionId(String.valueOf(webhookData.getOrderCode()))
                    .orElse(null);

            // If not a wallet transaction, check if it's an operational payment
            if (transaction == null) {
                return processOperationalPaymentWebhook(webhookData, webhookBody);
            }

            if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                log.info("Transaction already completed - orderCode: {}, ignoring duplicate webhook",
                        webhookData.getOrderCode());
                return true;
            }

            if (transaction.getStatus() == TransactionStatus.FAILED) {
                log.info("Transaction already marked as failed - orderCode: {}, ignoring webhook",
                        webhookData.getOrderCode());
                return true;
            }

            // Update with webhook data
            transaction.setGatewayResponse(webhookBody);
            transaction.setProcessedAt(LocalDateTime.now().plusHours(7));

            // Check if payment is successful
            // PayOS webhook code: "00" = success
            if ("00".equals(webhookData.getCode())) {
                // Update wallet balance
                Wallet wallet = walletRepository.findById(transaction.getWalletId())
                        .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, transaction.getWalletId()));

                BigDecimal oldBalance = wallet.getBalance();
                wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
                walletRepository.save(wallet);

                // Update transaction status
                transaction.setStatus(TransactionStatus.COMPLETED);

                log.info("✓ PayOS payment completed - orderCode: {}, amount: {}, wallet balance: {} -> {}",
                        webhookData.getOrderCode(), transaction.getAmount(), oldBalance, wallet.getBalance());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                log.warn("✗ PayOS payment failed - orderCode: {}, code: {}, desc: {}",
                        webhookData.getOrderCode(), webhookData.getCode(), webhookData.getDesc());
            }

            // Save transaction for both success and failure cases
            walletTransactionRepository.save(transaction);

            return true;
        } catch (AppException e) {
            log.error("Business error processing PayOS webhook: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.WEBHOOK_PROCESS_FAILED, e.getMessage());
        }
    }

    @Override
    public PaymentCancelResponse cancelPayment(Long orderCode) {
        WalletTransaction walletTransaction = walletTransactionRepository.findByExternalTransactionId(String.valueOf(orderCode))
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND, orderCode));


        if (walletTransaction.getStatus() != TransactionStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID, "Cannot cancel payment with status: " + walletTransaction.getStatus());
        }

        String reason = "Cancel";
        try {
            payOS.paymentRequests().cancel(orderCode, reason);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANCEL_FAILED, "Failed to cancel with PayOS: " + e.getMessage());
        }


        String jsonResponse = String.format(
                "{\"status\":\"cancelled\",\"reason\":\"%s\",\"cancelledBy\":\"user\",\"timestamp\":\"%s\"}",
                reason.replace("\"", "\\\""), // Escape quotes
                LocalDateTime.now().plusHours(7)
        );
        walletTransaction.setStatus(TransactionStatus.CANCELLED);
        walletTransaction.setMetadata(jsonResponse);
        walletTransaction.setProcessedAt(LocalDateTime.now().plusHours(7));
        walletTransactionRepository.save(walletTransaction);

        return PaymentCancelResponse.builder()
                .message("Canceled payment successfully")
                .orderCode(orderCode)
                .reason(reason)
                .build();
    }

    @Override
    public PaymentStatusResponse retrievePaymentStatus(Long orderCode) {
        WalletTransaction walletTransaction = walletTransactionRepository.findByExternalTransactionId(String.valueOf(orderCode))
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND, orderCode));
        return PaymentStatusResponse.builder()
                .amount(walletTransaction.getAmount())
                .orderCode(orderCode)
                .transactionStatus(walletTransaction.getStatus())
                .build();


    }

    @Override
    public Payout retrievePayout(PayoutRequests payoutRequests) {
        try {
            if (payoutRequests.getReferenceId().isEmpty()) {
                payoutRequests.setReferenceId("payout_" + (System.currentTimeMillis() / 1000));
            }
            // Use payOSPayout bean for payout operations
            return payOSPayout.payouts().create(payoutRequests);
        } catch (Exception e) {
            log.error("create payout failed", e);
            return null;
        }
    }

    @Override
    public Payout getPayout(String payoutId) {
        try {
            // Use payOSPayout bean for payout operations
            return payOSPayout.payouts().get(payoutId);
        } catch (Exception e) {
            log.error("Failed to get payout status for payoutId: {}", payoutId, e);
            throw new AppException(ErrorCode.PAYOUT_STATUS_CHECK_FAILED, payoutId);
        }
    }

    @Override
    @Transactional
    public Boolean processPayoutWebhook(String webhookBody, String signature) {
        try {
            log.info("Processing PayOS payout webhook - signature present: {}", signature != null);
            log.debug("Raw payout webhook body: {}", webhookBody);

            // Verify webhook signature and parse data
            WebhookData webhookData = payOS.webhooks().verify(webhookBody);

            log.info("PayOS payout webhook verified - referenceId: {}, code: {}",
                    webhookData.getReference(), webhookData.getCode());

            String referenceId = webhookData.getReference();

            // Find transaction by referenceId (which we stored as externalTransactionId)
            WalletTransaction transaction = walletTransactionRepository
                    .findByExternalTransactionId(referenceId)
                    .orElse(null);

            if (transaction == null) {
                log.warn("Payout transaction not found for referenceId: {} - This might be a test webhook",
                        referenceId);
                return true;
            }

            if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                log.info("Payout transaction already completed - referenceId: {}, ignoring duplicate webhook",
                        referenceId);
                return true;
            }

            if (transaction.getStatus() == TransactionStatus.FAILED) {
                log.info("Payout transaction already marked as failed - referenceId: {}, ignoring webhook",
                        referenceId);
                return true;
            }

            // Update with webhook data
            transaction.setGatewayResponse(webhookBody);
            transaction.setProcessedAt(LocalDateTime.now().plusHours(7));

            String status = webhookData.getCode();

            if ("SUCCESSFUL".equals(status)) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                log.info("✓ Payout completed successfully - referenceId: {}, amount: {}",
                        referenceId, transaction.getAmount());
            } else if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                // Hoàn tiền vào ví khi payout thất bại
                refundFailedPayout(transaction);
                log.warn("✗ Payout failed/cancelled - referenceId: {}, status: {}, refunded to wallet",
                        referenceId, status);
            } else {
                // PROCESSING or other status - keep as pending
                transaction.setStatus(TransactionStatus.PROCESSING);
                log.info("Payout still processing - referenceId: {}, status: {}", referenceId, status);
            }

            walletTransactionRepository.save(transaction);
            return true;

        } catch (AppException e) {
            log.error("Business error processing payout webhook: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing payout webhook: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.WEBHOOK_PROCESS_FAILED, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void checkPendingPayouts() {
        // Check both PENDING and PROCESSING payouts
        List<WalletTransaction> pendingPayouts = walletTransactionRepository
                .findByTransactionTypeAndStatus(TransactionType.CASH_OUT, TransactionStatus.PENDING);

        List<WalletTransaction> processingPayouts = walletTransactionRepository
                .findByTransactionTypeAndStatus(TransactionType.CASH_OUT, TransactionStatus.PROCESSING);

        // Combine both lists
        List<WalletTransaction> allPendingPayouts = new ArrayList<>();
        allPendingPayouts.addAll(pendingPayouts);
        allPendingPayouts.addAll(processingPayouts);

        if (allPendingPayouts.isEmpty()) {
            log.debug("No pending payouts to check");
            return;
        }

        log.info("Checking {} pending/processing payout(s) - PENDING: {}, PROCESSING: {}",
                allPendingPayouts.size(), pendingPayouts.size(), processingPayouts.size());

        for (WalletTransaction transaction : allPendingPayouts) {
            try {
                checkPayoutStatus(transaction);
            } catch (Exception e) {
                log.error("Error checking payout status for transaction: {}", transaction.getId(), e);
            }
        }
    }

    /**
     * Helper method to check individual payout status
     */
    private void checkPayoutStatus(WalletTransaction transaction) {
        try {
            log.debug("Checking payout status - transactionId: {}, metadata: '{}'",
                    transaction.getId(), transaction.getMetadata());

            // Try to get PayOS batch ID from metadata first (for new transactions)
            String payoutId = extractPayoutIdFromMetadata(transaction.getMetadata());

            // If no payoutId in metadata, use referenceId (for old transactions)
            if (payoutId == null) {
                payoutId = transaction.getExternalTransactionId();
                log.info("No batch ID in metadata - Using referenceId (mã chi) for status check: {}", payoutId);
            } else {
                log.info("Found batch ID in metadata - Using payoutId for status check: {}", payoutId);
            }

            Payout payout = getPayout(payoutId);

            // PayOS returns transactions array, get the first transaction state
            String approvalState = String.valueOf(payout.getApprovalState());
            String transactionState = null;

            if (!payout.getTransactions().isEmpty()) {
                transactionState = String.valueOf(payout.getTransactions().get(0).getState());
            }

            log.info("Payout status check - referenceId: {}, approvalState: {}, transactionState: {}",
                    payoutId, approvalState, transactionState);

            transaction.setProcessedAt(LocalDateTime.now().plusHours(7));

            // Check transaction state first (more accurate), fallback to approval state
            String status = transactionState != null ? transactionState : approvalState;

            if ("SUCCEEDED".equals(status) || "SUCCESSFUL".equals(status) || "SUCCESS".equals(status)) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                log.info("✓ Payout completed via polling - referenceId: {}, amount: {}",
                        payoutId, transaction.getAmount());
            } else if ("FAILED".equals(status) || "CANCELLED".equals(status) || "REJECTED".equals(status)) {
                refundFailedPayout(transaction);
                log.warn("✗ Payout failed via polling - referenceId: {}, status: {}, refunded to wallet",
                        payoutId, status);
            } else if ("PROCESSING".equals(status) || "PENDING".equals(status)) {
                transaction.setStatus(TransactionStatus.PROCESSING);
                log.info("Payout still processing - referenceId: {}", payoutId);
            }

            walletTransactionRepository.save(transaction);

        } catch (AppException e) {
            // If payout not found (old transactions without batch ID), check if it's been too long
            if (e.getErrorCode() == ErrorCode.PAYOUT_STATUS_CHECK_FAILED) {
                handleOldTransactionWithoutBatchId(transaction);
            } else {
                log.error("Error checking payout status for transaction: {}", transaction.getId(), e);
            }
        } catch (vn.payos.exception.APIException e) {
            // PayOS API error - likely payout not found (using referenceId instead of batch ID)
            log.warn("PayOS API error: {} - Handling as old transaction", e.getMessage());
            handleOldTransactionWithoutBatchId(transaction);
        } catch (Exception e) {
            log.error("Error checking payout status for transaction: {}", transaction.getId(), e);
        }
    }

    /**
     * Handle old transactions that don't have batch ID in metadata
     * If transaction has been PROCESSING for more than 1 hour, assume it succeeded
     */
    private void handleOldTransactionWithoutBatchId(WalletTransaction transaction) {
        log.warn("Cannot check payout status for old transaction without batch ID: {}, referenceId: {}",
                transaction.getId(), transaction.getExternalTransactionId());

        // If transaction has been processing for more than 1 hour, likely succeeded
        if (transaction.getCreatedAt() != null) {
            long minutesElapsed = java.time.Duration.between(transaction.getCreatedAt(), LocalDateTime.now().plusHours(7)).toMinutes();

            if (minutesElapsed >= 60) {
                // Assume success after 1 hour
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setProcessedAt(LocalDateTime.now().plusHours(7));

                String note = String.format(
                        "{\"note\":\"Auto-completed old transaction after %d minutes (no batch ID for status check)\"}",
                        minutesElapsed
                );
                transaction.setMetadata(note);

                walletTransactionRepository.save(transaction);

                log.info("✓ Auto-completed old payout transaction - referenceId: {}, elapsed: {} minutes",
                        transaction.getExternalTransactionId(), minutesElapsed);
            } else {
                log.info("Old transaction still within 1 hour window - referenceId: {}, elapsed: {} minutes",
                        transaction.getExternalTransactionId(), minutesElapsed);
            }
        }
    }

    /**
     * Helper method to extract PayOS batch ID from JSON metadata
     */
    private String extractPayoutIdFromMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            // Simple JSON parsing - extract "payoutId" value
            // Format: {"payoutId":"batch_xxx",...} or {"payoutId": "batch_xxx",...}
            // Handle both with and without space after colon

            int keyIndex = metadata.indexOf("\"payoutId\"");
            if (keyIndex == -1) {
                return null;
            }

            // Find the opening quote of the value
            int startQuote = metadata.indexOf("\"", keyIndex + "\"payoutId\"".length());
            if (startQuote == -1) {
                return null;
            }

            // Find the closing quote
            int endQuote = metadata.indexOf("\"", startQuote + 1);
            if (endQuote == -1) {
                return null;
            }

            String payoutId = metadata.substring(startQuote + 1, endQuote);
            log.debug("Extracted batch ID from metadata: {}", payoutId);
            return payoutId;

        } catch (Exception e) {
            log.error("Failed to extract payoutId from metadata: {}", metadata, e);
            return null;
        }
    }

    /**
     * Helper method to refund failed payout back to wallet
     * Note: Removed @Transactional as this is a private method called within a transactional context
     */
    private void refundFailedPayout(WalletTransaction transaction) {
        // Update transaction status to failed
        transaction.setStatus(TransactionStatus.FAILED);

        // Find wallet and refund the amount
        Wallet wallet = walletRepository.findById(transaction.getWalletId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND, transaction.getWalletId()));

        BigDecimal oldBalance = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(wallet);

        log.info("Refunded failed payout to wallet - walletId: {}, amount: {}, balance: {} -> {}",
                wallet.getId(), transaction.getAmount(), oldBalance, wallet.getBalance());
    }

    /**
     * Helper method to process operational payment webhook
     */
    private Boolean processOperationalPaymentWebhook(WebhookData webhookData, String webhookBody) {
        log.info("Processing operational payment webhook - orderCode: {}", webhookData.getOrderCode());

        // Find operational payment by payment transaction ID
        OperationalPaymentEntity payment = operationalPaymentRepository
                .findByPaymentTransactionId(String.valueOf(webhookData.getOrderCode()))
                .orElse(null);

        if (payment == null) {
            log.warn("Operational payment not found for orderCode: {} - This might be a test webhook",
                    webhookData.getOrderCode());
            return true;
        }

        // Check if already processed
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Operational payment already marked as PAID - orderCode: {}, ignoring duplicate webhook",
                    webhookData.getOrderCode());
            return true;
        }

        // Check if payment is successful (PayOS code "00" = success)
        if ("00".equals(webhookData.getCode())) {
            log.info("✓ Operational payment webhook successful - lotId: {}, amount: {}",
                    payment.getLotId(), payment.getTotalFee());

            // Update payment status to PAID directly (avoid circular dependency)
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now().plusHours(7));
            payment.setNotes("Payment confirmed via PayOS webhook");
            operationalPaymentRepository.save(payment);

            log.info("Operational payment {} confirmed successfully for lot: {}",
                    payment.getId(), payment.getLotId());

            // Call parking-lot-service to activate the lot
            try {
                parkingLotClient.activateParkingLot(payment.getLotId());
                log.info("Successfully activated parking lot: {}", payment.getLotId());
            } catch (Exception e) {
                log.error("Failed to activate parking lot {} after payment confirmation", payment.getLotId(), e);
                // Payment is still marked as PAID, but lot activation failed
                // This should trigger manual intervention or retry mechanism
            }

        } else {
            log.warn("✗ Operational payment webhook failed - lotId: {}, code: {}, desc: {}",
                    payment.getLotId(), webhookData.getCode(), webhookData.getDesc());

            // Mark payment as cancelled directly
            payment.setPaymentStatus(PaymentStatus.CANCELLED);
            payment.setNotes("Payment failed via PayOS webhook: " + webhookData.getDesc());
            operationalPaymentRepository.save(payment);
        }

        return true;
    }
}
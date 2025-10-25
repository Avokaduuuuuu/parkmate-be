package com.parkmate.payos;

import com.parkmate.client.UserServiceClient;
import com.parkmate.common.QRCodeGenerator;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.payos.dto.PaymentCancelResponse;
import com.parkmate.payos.dto.PaymentStatusResponse;
import com.parkmate.wallet.Wallet;
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
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final QRCodeGenerator qrCodeGenerator;
    private final UserServiceClient userServiceClient;

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

            Long accountId = Long.parseLong(userHeadId);

            Long userId = userServiceClient.getUserIdByAccountId(accountId);

            // Find wallet
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

            // Generate unique orderCode
            long orderCode = System.currentTimeMillis();

            // Create item data (required by PayOS)
            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name("Top up wallet")
                    .quantity(1)
                    .price(amount)
                    .build();

            // Create payment link request
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
                    .userId(accountId)
                    .walletId(wallet.getId())
                    .transactionType(TransactionType.TOP_UP)
                    .amount(BigDecimal.valueOf(amount))
                    .fee(BigDecimal.ZERO)
                    .netAmount(BigDecimal.valueOf(amount))
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
    public Boolean processWebhook(String webhookBody, String signature) {
        try {
            // Log raw webhook for debugging
            log.info("Processing PayOS webhook - signature present: {}", signature != null);
            log.debug("Raw webhook body: {}", webhookBody);

            // Verify webhook signature and parse data
            WebhookData webhookData = payOS.webhooks().verify(webhookBody);

            log.info("PayOS webhook verified - orderCode: {}, code: {}, amount: {}",
                    webhookData.getOrderCode(), webhookData.getCode(), webhookData.getAmount());

            // Find transaction by orderCode
            WalletTransaction transaction = walletTransactionRepository
                    .findByExternalTransactionId(String.valueOf(webhookData.getOrderCode()))
                    .orElse(null);

            // Handle test webhooks or transactions not found
            if (transaction == null) {
                log.warn("Transaction not found for orderCode: {} - This might be a test webhook or invalid order",
                        webhookData.getOrderCode());
                // Return true to acknowledge test webhooks without throwing error
                // PayOS expects 200 OK even for test webhooks
                return true;
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
            transaction.setProcessedAt(LocalDateTime.now());

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
    public PaymentCancelResponse cancelPayment(Long orderCode, String reason) {
        WalletTransaction walletTransaction = walletTransactionRepository.findByExternalTransactionId(String.valueOf(orderCode))
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND, orderCode));


        if (walletTransaction.getStatus() != TransactionStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID, "Cannot cancel payment with status: " + walletTransaction.getStatus());
        }


        try {
            payOS.paymentRequests().cancel(orderCode, reason);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CANCEL_FAILED, "Failed to cancel with PayOS: " + e.getMessage());
        }


        String jsonResponse = String.format(
                "{\"status\":\"cancelled\",\"reason\":\"%s\",\"cancelledBy\":\"user\",\"timestamp\":\"%s\"}",
                reason.replace("\"", "\\\""), // Escape quotes
                LocalDateTime.now()
        );
        walletTransaction.setStatus(TransactionStatus.CANCELLED);
        walletTransaction.setMetadata(jsonResponse);
        walletTransaction.setProcessedAt(LocalDateTime.now());
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
}
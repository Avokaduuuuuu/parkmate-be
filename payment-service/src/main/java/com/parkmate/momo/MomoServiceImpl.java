package com.parkmate.momo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.config.MomoConfig;
import com.parkmate.momo.dto.MoMoIPNRequest;
import com.parkmate.momo.dto.MoMoPaymentRequest;
import com.parkmate.momo.dto.MoMoPaymentResponse;
import com.parkmate.wallet.Wallet;
import com.parkmate.wallet.WalletRepository;
import com.parkmate.walletTransaction.TransactionStatus;
import com.parkmate.walletTransaction.TransactionType;
import com.parkmate.walletTransaction.WalletTransaction;
import com.parkmate.walletTransaction.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoServiceImpl implements MomoService {

    private final MomoConfig momoConfig;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MoMoPaymentResponse create(Long userId, Long amount, String orderInfo) {
        validateRequest(userId, amount);

        // Find wallet for user
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        // Generate unique IDs
        UUID sessionId = UuidCreator.getTimeOrderedEpoch();
        String requestId = sessionId.toString();
        String orderId = "ORDER_" + System.currentTimeMillis();

        // Build MoMo payment request
        MoMoPaymentRequest momoRequest = MoMoPaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .accessKey(momoConfig.getAccessKey())
                .requestId(requestId)
                .amount(String.valueOf(amount))
                .orderId(orderId)
                .orderInfo(orderInfo != null ? orderInfo : "Top up wallet")
                .redirectUrl(momoConfig.getRedirectUrl())
                .ipnUrl(momoConfig.getIpnUrl())
                .requestType(momoConfig.getRequestType())
                .extraData("")
                .lang(momoConfig.getLang())
                .signature("")
                .build();

        // Set signature after building
        String signature = createSignature(momoRequest);
        momoRequest.setSignature(signature);

        try {
            // Create pending transaction
            WalletTransaction transaction = WalletTransaction.builder()
                    .userId(userId)
                    .walletId(wallet.getId())
                    .sessionId(sessionId)
                    .transactionType(TransactionType.TOP_UP)
                    .amount(BigDecimal.valueOf(amount))
                    .fee(BigDecimal.ZERO)
                    .netAmount(BigDecimal.valueOf(amount))
                    .externalTransactionId(orderId)
                    .status(TransactionStatus.PENDING)
                    .description(orderInfo)
                    .build();

            walletTransactionRepository.save(transaction);

            // Call MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MoMoPaymentRequest> entity = new HttpEntity<>(momoRequest, headers);

            ResponseEntity<MoMoPaymentResponse> response = restTemplate.exchange(
                    momoConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    MoMoPaymentResponse.class
            );

            MoMoPaymentResponse momoResponse = response.getBody();

            if (momoResponse != null && !momoResponse.isSuccess()) {
                // Update transaction status to FAILED
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setGatewayResponse(objectMapper.writeValueAsString(momoResponse));
                walletTransactionRepository.save(transaction);
            }

            return momoResponse;

        } catch (Exception e) {
            log.error("Error creating MoMo payment for userId: {}, amount: {}", userId, amount, e);
            throw new RuntimeException("Failed to create MoMo payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean processIPNCallback(MoMoIPNRequest request) {
        try {
            // Verify signature first
            if (!verifyIPNSignature(request)) {
                log.error("Invalid IPN signature for orderId: {}", request.getOrderId());
                return false;
            }

            // Find transaction by orderId (external_transaction_id)
            WalletTransaction transaction = walletTransactionRepository
                    .findByExternalTransactionId(request.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + request.getOrderId()));

            // Update transaction with MoMo transId
            transaction.setExternalTransactionId(String.valueOf(request.getTransId()));
            transaction.setGatewayResponse(objectMapper.writeValueAsString(request));
            transaction.setProcessedAt(LocalDateTime.now());

            if (request.isSuccess()) {
                // Update wallet balance
                Wallet wallet = walletRepository.findById(transaction.getWalletId())
                        .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + transaction.getWalletId()));

                wallet.setBalance(wallet.getBalance() + request.getAmount());
                wallet.setUpdatedAt(LocalDateTime.now());
                walletRepository.save(wallet);

                // Update transaction status
                transaction.setStatus(TransactionStatus.COMPLETED);
                log.info("MoMo payment completed successfully for orderId: {}, transId: {}",
                        request.getOrderId(), request.getTransId());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                log.warn("MoMo payment failed for orderId: {}, resultCode: {}, message: {}",
                        request.getOrderId(), request.getResultCode(), request.getMessage());
            }

            walletTransactionRepository.save(transaction);
            return true;

        } catch (Exception e) {
            log.error("Error processing MoMo IPN callback for orderId: {}", request.getOrderId(), e);
            return false;
        }
    }

    @Override
    public boolean verifyIPNSignature(MoMoIPNRequest ipnRequest) {
        try {
            Map<String, String> params = new java.util.HashMap<>();
            params.put("accessKey", momoConfig.getAccessKey());
            params.put("amount", String.valueOf(ipnRequest.getAmount()));
            params.put("extraData", ipnRequest.getExtraData() != null ? ipnRequest.getExtraData() : "");
            params.put("message", ipnRequest.getMessage());
            params.put("orderId", ipnRequest.getOrderId());
            params.put("orderInfo", ipnRequest.getOrderInfo());
            params.put("orderType", ipnRequest.getOrderType());
            params.put("partnerCode", ipnRequest.getPartnerCode());
            params.put("payType", ipnRequest.getPayType());
            params.put("requestId", ipnRequest.getRequestId());
            params.put("responseTime", String.valueOf(ipnRequest.getResponseTime()));
            params.put("resultCode", String.valueOf(ipnRequest.getResultCode()));
            params.put("transId", String.valueOf(ipnRequest.getTransId()));

            boolean isValid = MomoUtil.verifySignature(ipnRequest.getSignature(), params, momoConfig.getSecretKey());

            if (!isValid) {
                log.error("Signature verification failed for orderId: {}", ipnRequest.getOrderId());
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying IPN signature", e);
            return false;
        }
    }

    @Override
    public String createSignature(MoMoPaymentRequest request) {
        try {
            Map<String, String> params = new java.util.HashMap<>();
            params.put("accessKey", request.getAccessKey());
            params.put("amount", request.getAmount());
            params.put("extraData", request.getExtraData() != null ? request.getExtraData() : "");
            params.put("ipnUrl", request.getIpnUrl());
            params.put("orderId", request.getOrderId());
            params.put("orderInfo", request.getOrderInfo());
            params.put("partnerCode", request.getPartnerCode());
            params.put("redirectUrl", request.getRedirectUrl());
            params.put("requestId", request.getRequestId());
            params.put("requestType", request.getRequestType());

            String rawSignature = MomoUtil.createRawSignature(params);
            return MomoUtil.hmacSHA256(rawSignature, momoConfig.getSecretKey());

        } catch (Exception e) {
            log.error("Error creating signature", e);
            throw new RuntimeException("Failed to create signature", e);
        }
    }

    private void validateRequest(Long userId, Long amount) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}

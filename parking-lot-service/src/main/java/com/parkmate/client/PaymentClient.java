package com.parkmate.client;

import com.parkmate.client.request.CreateOperationalPaymentRequest;
import com.parkmate.client.request.CreateTransactionRequest;
import com.parkmate.client.request.CreateWalletRequest;
import com.parkmate.client.response.OperationalPaymentResponse;
import com.parkmate.client.response.WalletResponse;
import com.parkmate.client.response.WalletTransactionResponse;
import com.parkmate.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/v1/payment-service/wallets")
    ResponseEntity<ApiResponse<WalletResponse>> createWallet(@RequestBody CreateWalletRequest request);


    @GetMapping("/api/v1/payment-service/wallets")
    ResponseEntity<ApiResponse<WalletResponse>> getPayment();

    @PostMapping("/api/v1/internal/wallet-transactions")
    ResponseEntity<ApiResponse<WalletTransactionResponse>> deductWallet(@RequestBody CreateTransactionRequest walletTransaction);

    /**
     * Creates an operational fee payment for parking lot activation
     * This is called when a partner configures their parking lot and triggers payment
     *
     * @param request Contains lotId, partnerId, and lotAreaSqm
     * @return Payment response with payment link and QR code
     */
    @PostMapping("/api/v1/payment-service/operational-payments")
    ResponseEntity<ApiResponse<OperationalPaymentResponse>> createOperationalPayment(@RequestBody CreateOperationalPaymentRequest request);
}

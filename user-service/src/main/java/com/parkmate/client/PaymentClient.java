package com.parkmate.client;

import com.parkmate.client.dto.request.CreateTransactionRequest;
import com.parkmate.client.dto.request.CreateWalletRequest;
import com.parkmate.client.dto.response.WalletResponse;
import com.parkmate.client.dto.response.WalletTransactionResponse;
import com.parkmate.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/v1/payment-service/wallets")
    ResponseEntity<ApiResponse<WalletResponse>> createPayment(@RequestBody CreateWalletRequest request);


    @GetMapping("/api/v1/payment-service/wallets")
    ResponseEntity<ApiResponse<WalletResponse>> getPayment();

    @PostMapping("/api/v1/internal/wallet-transactions")
    ResponseEntity<ApiResponse<WalletTransactionResponse>> deductWallet(@RequestBody CreateTransactionRequest walletTransaction);
}

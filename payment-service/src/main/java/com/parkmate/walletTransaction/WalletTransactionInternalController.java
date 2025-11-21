package com.parkmate.walletTransaction;

import com.parkmate.common.ApiResponse;
import com.parkmate.walletTransaction.dto.CreateTransactionRequest;
import com.parkmate.walletTransaction.dto.SessionPaymentCreateRequest;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/wallet-transactions")
@RequiredArgsConstructor
public class WalletTransactionInternalController {

    private final WalletTransactionService walletTransactionService;

    @PostMapping
    @Hidden
    public ResponseEntity<ApiResponse<?>> deductWallet(@RequestBody CreateTransactionRequest walletTransaction) {
        return ResponseEntity.ok(ApiResponse.success(walletTransactionService.createWalletTransaction(walletTransaction)));
    }

    @PostMapping("/refund")
    @Hidden
    public ResponseEntity<ApiResponse<?>> refundWallet(@RequestBody CreateTransactionRequest walletTransaction) {
        return ResponseEntity.ok(ApiResponse.success(walletTransactionService.createWalletTransaction(walletTransaction)));
    }

    @PostMapping("/session-payment")
    @Hidden
    public ResponseEntity<ApiResponse<?>> deductWalletAtGate(@RequestBody SessionPaymentCreateRequest sessionPaymentCreateRequest) {
        walletTransactionService.createSessionCharge(sessionPaymentCreateRequest);
        return ResponseEntity.ok(ApiResponse.success("Payment successful"));
    }

}

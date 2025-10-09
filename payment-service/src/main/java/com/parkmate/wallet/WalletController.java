package com.parkmate.wallet;

import com.parkmate.common.ApiResponse;
import com.parkmate.wallet.dto.CreateWalletRequest;
import com.parkmate.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "Endpoints for wallet management")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.createWallet(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WalletResponse>>> getWallets(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sortBy,
            @RequestParam String sortOrder,
            @RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userHeaderId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getAll(page, size, sortBy, sortOrder, userHeaderId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserId(
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        return ResponseEntity.ok(
                ApiResponse.success(walletService.getByUserId(userIdHeader)));
    }
}

package com.parkmate.wallet;

import com.parkmate.common.ApiResponse;
import com.parkmate.wallet.dto.CreateWalletRequest;
import com.parkmate.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(walletService.createWallet(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WalletResponse>>> getWalletById(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sortBy,
            @RequestParam String sortOrder,
            @RequestHeader(value = "X-User-Id") @Parameter(hidden = true, required = false) String userHeaderId,
            @RequestParam(required = false) Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getAll(page, size, sortBy, sortOrder, userHeaderId)));
    }

}

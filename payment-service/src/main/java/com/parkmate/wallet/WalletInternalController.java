package com.parkmate.wallet;

import com.parkmate.common.ApiResponse;
import com.parkmate.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/wallets")
@RequiredArgsConstructor
public class WalletInternalController {

    private final WalletService walletService;

    @GetMapping("/me")
    @Hidden
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserId(
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) @Parameter(hidden = true) String userRoleHeader) {
        return ResponseEntity.ok(
                ApiResponse.success(walletService.getByUserId(userIdHeader, userRoleHeader)));
    }

    /**
     * Internal endpoint for other services to get wallet by userId
     * Used by user-service for wallet balance checks
     */
    @GetMapping("/{userId}")
    @Hidden
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserIdInternal(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(walletService.getByUserId(String.valueOf(userId), "MEMBER")));
    }
}

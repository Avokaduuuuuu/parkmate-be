package com.parkmate.wallet;

import com.parkmate.common.ApiResponse;
import com.parkmate.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        return ResponseEntity.ok(
                ApiResponse.success(walletService.getByUserId(userIdHeader)));
    }

}

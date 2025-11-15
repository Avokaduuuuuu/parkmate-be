package com.parkmate.wallet;

import com.parkmate.common.ApiResponse;
import com.parkmate.wallet.dto.CreateWalletRequest;
import com.parkmate.wallet.dto.WalletResponse;
import com.parkmate.wallet.dto.WithdrawalRequest;
import com.parkmate.wallet.dto.WithdrawalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
            @RequestParam String sortOrder) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getAll(page, size, sortBy, sortOrder)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserId(
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        return ResponseEntity.ok(
                ApiResponse.success(walletService.getByUserId(userIdHeader)));
    }

    @GetMapping("/sync")
    public ResponseEntity<ApiResponse<Map<Long, BigDecimal>>> syncWallets(@RequestParam List<Long> userIds) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getUserWallets(userIds)));
    }

    @PostMapping("/withdraw")
    @Operation(
            summary = "Request withdrawal",
            description = """
                    Partner requests to withdraw money from their wallet to bank account.
                    
                    **Requirements:**
                    - Minimum withdrawal: 10,000 VND
                    - Maximum withdrawal: 50,000,000 VND
                    - Wallet must have sufficient balance
                    - Valid bank account information required
                    
                    **Process:**
                    1. Wallet balance is deducted immediately
                    2. Payout request is sent to PayOS
                    3. Partner is notified when payout completes
                    4. If payout fails, amount is automatically refunded to wallet
                    
                    **Bank BIN/Codes (examples):**
                    - 970436: Vietcombank (VCB)
                    - 970407: Techcombank (TCB)
                    - 970422: MBBank (MB)
                    - 970416: ACB
                    - 970432: VPBank
                    - 970403: Sacombank
                    - 970415: Vietinbank
                    
                    **Note:** Use the 6-digit BIN code, not the short name.
                    """
    )
    public ResponseEntity<ApiResponse<WithdrawalResponse>> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequest request,
            @RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userIdHeader) {
        return ResponseEntity.ok(ApiResponse.success(
                walletService.requestWithdrawal(userIdHeader, request),
                "Withdrawal request submitted successfully"
        ));
    }
}

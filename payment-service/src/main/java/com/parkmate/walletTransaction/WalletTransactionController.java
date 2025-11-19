package com.parkmate.walletTransaction;

import com.parkmate.common.ApiResponse;
import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.parkmate.walletTransaction.dto.WalletTransactionResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/transactions")
@RequiredArgsConstructor
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;


    @GetMapping
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getWalletTransactions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) @Parameter(hidden = true) String role,
            @ModelAttribute TransactionSearchCriteria criteria
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(walletTransactionService.getTransactions(
                        page, size, sortBy, sortOrder, criteria, userIdHeader, role
                ))
        );
    }

}

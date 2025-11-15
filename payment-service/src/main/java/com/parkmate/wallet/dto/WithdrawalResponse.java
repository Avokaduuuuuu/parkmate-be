package com.parkmate.wallet.dto;

import com.parkmate.walletTransaction.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for withdrawal requests
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalResponse {

    private String referenceId;
    private BigDecimal amount;
    private TransactionStatus status;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime requestedAt;
    private String message;
}

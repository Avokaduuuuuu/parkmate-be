package com.parkmate.walletTransaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WalletTransactionResponse {
    Long userId;
    Long walletId;
    UUID sessionId;
    String transactionType; // String instead of enum
    BigDecimal amount;
    BigDecimal fee;
    BigDecimal netAmount;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    String externalTransactionId;
    String gatewayResponse;
    String status; // String instead of enum
    LocalDateTime processedAt;
    String description;
    String metadata;
    LocalDateTime createdAt;
}

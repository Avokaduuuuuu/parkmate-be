package com.parkmate.client.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    UUID id;
    Long walletId;
    String transactionType;
    BigDecimal amount;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    String externalTransactionId;
    String gatewayResponse;
    String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime processedAt;
    String description;
    String metadata;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
}

package com.parkmate.walletTransaction.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "User ID is required")
    Long userId;
    Long partnerId;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount;
    @NotBlank(message = "Transaction type is required")
    String transactionType; // "TOP_UP", "DEDUCTION", "REFUND", "REVERSAL", "PENALTY", "SUBSCRIPTION"
    @Nullable
    Long reservationId;
    @Nullable
    Long subscriptionId;
    LocalDateTime processedAt;
    String description;


}

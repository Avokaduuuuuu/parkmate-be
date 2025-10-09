package com.parkmate.walletTransaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "User ID is required")
    Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    String transactionType; // "TOP_UP", "DEDUCTION", "REFUND", "REVERSAL", "PENALTY", "SUBSCRIPTION"

    String description;

}

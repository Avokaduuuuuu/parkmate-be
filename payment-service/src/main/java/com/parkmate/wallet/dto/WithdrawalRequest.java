package com.parkmate.wallet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for withdrawal
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum withdrawal amount is 10,000 VND")
    private BigDecimal amount;

    @NotNull(message = "Bank account information is required")
    @Valid
    private BankAccountInfo bankAccount;
}

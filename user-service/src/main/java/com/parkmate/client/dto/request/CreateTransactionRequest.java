package com.parkmate.client.dto.request;

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

    private Long userId;
    private BigDecimal amount;
    private String action;           // "DEDUCT", "DEPOSIT", "REFUND" (String thay vì enum)
    private String transactionType;  // "DEBIT", "CREDIT"
    private String referenceId;
    private String description;

}

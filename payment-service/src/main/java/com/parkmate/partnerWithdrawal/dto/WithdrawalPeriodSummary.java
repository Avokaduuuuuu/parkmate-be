package com.parkmate.partnerWithdrawal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalPeriodSummary {
    private String start;
    private String end;
    private BigDecimal amount;
}

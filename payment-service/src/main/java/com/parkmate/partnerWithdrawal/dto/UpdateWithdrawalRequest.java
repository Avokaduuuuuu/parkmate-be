package com.parkmate.partnerWithdrawal.dto;

import com.parkmate.partnerWithdrawal.WithdrawalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWithdrawalRequest {
    private WithdrawalStatus status;
    private String failureReason;
    private String description;
}

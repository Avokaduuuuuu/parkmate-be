package com.parkmate.partnerWithdrawal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWithdrawalRequest {

    @NotNull(message = "Lot ID is required")
    private Long lotId;

    @NotEmpty(message = "Period IDs cannot be empty")
    private List<Long> periodIds;

    @NotNull(message = "Bank account number is required")
    @Size(min = 1, max = 50, message = "Bank account number must be between 1 and 50 characters")
    private String bankAccountNumber;

    @NotNull(message = "Bank account name is required")
    @Size(min = 1, max = 255, message = "Bank account name must be between 1 and 255 characters")
    private String bankAccountName;

    @NotNull(message = "Bank code is required")
    @Size(min = 1, max = 20, message = "Bank code must be between 1 and 20 characters")
    private String bankCode;
}

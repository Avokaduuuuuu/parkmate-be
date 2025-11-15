package com.parkmate.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bank account information for withdrawal requests
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountInfo {

    @NotBlank(message = "Account number is required")
    @Schema(description = "Bank account number", example = "0123456789")
    private String accountNumber;

    @NotBlank(message = "Account name is required")
    @Schema(description = "Account holder name (for display purposes only)", example = "NGUYEN VAN A")
    private String accountName;

    @NotBlank(message = "Bank code is required")
    @Schema(description = "Bank BIN/code (e.g., 970436 for Vietcombank, 970407 for Techcombank)", example = "970436")
    private String bankCode;

    @Schema(description = "Optional description for the withdrawal", example = "Partner monthly withdrawal")
    private String description;
}

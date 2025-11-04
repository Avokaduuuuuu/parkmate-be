package com.parkmate.wallet.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


public record WalletResponse(
        Long holderId,
        String walletOwner,
        Long balance,
        String currency,
        boolean isActive,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt

) {
}

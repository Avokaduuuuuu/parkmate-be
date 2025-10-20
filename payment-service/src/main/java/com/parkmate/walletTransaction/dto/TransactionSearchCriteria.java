package com.parkmate.walletTransaction.dto;

import com.parkmate.walletTransaction.TransactionStatus;
import com.parkmate.walletTransaction.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class TransactionSearchCriteria {

    Boolean ownedByMe;

    UUID id;

    Long userId;

    Long walletId;

    UUID sessionId;

    TransactionType transactionType;

    TransactionStatus status;

    BigDecimal minAmount;

    BigDecimal maxAmount;

    LocalDateTime createdAfter;

    LocalDateTime createdBefore;

    LocalDateTime processedAfter;

    LocalDateTime processedBefore;

}

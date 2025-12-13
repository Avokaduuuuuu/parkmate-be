package com.parkmate.walletTransaction;

import com.parkmate.client.dto.ParkingLotBasicInfo;
import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TransactionSpecification {

    public static Predicate buildPredicate(TransactionSearchCriteria criteria, Long walletId) {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }
        
        // Filter by wallet ID from header (applies when ownedByMe is true)
        if (walletId != null && Boolean.TRUE.equals(criteria.getOwnedByMe())) {
            builder.and(transaction.walletId.eq(walletId));
        }
        // Filter by specific user ID (only when ownedByMe is explicitly false - for admin use)
        else if (criteria.getUserId() != null && Boolean.FALSE.equals(criteria.getOwnedByMe())) {
            builder.and(transaction.wallet.holderId.eq(criteria.getUserId()));
        }
        // Filter by user ID when ownedByMe is null
        else if (criteria.getUserId() != null && criteria.getOwnedByMe() == null) {
            builder.and(transaction.wallet.holderId.eq(criteria.getUserId()));
        }
        // Default: when walletId from header is present and ownedByMe is null, filter by header walletId
        else if (walletId != null && criteria.getOwnedByMe() == null) {
            builder.and(transaction.walletId.eq(walletId));
        }

        // Filter by transaction ID
        if (criteria.getId() != null) {
            builder.and(transaction.id.eq(criteria.getId()));
        }

        // Filter by wallet ID from criteria
        if (criteria.getWalletId() != null) {
            builder.and(transaction.walletId.eq(criteria.getWalletId()));
        }


        // Filter by transaction type
        if (criteria.getTransactionType() != null) {
            builder.and(transaction.transactionType.eq(criteria.getTransactionType()));
        }

        // Filter by status
        if (criteria.getStatus() != null) {
            builder.and(transaction.status.eq(criteria.getStatus()));
        }

        // Filter by minimum amount
        if (criteria.getMinAmount() != null) {
            builder.and(transaction.amount.goe(criteria.getMinAmount()));
        }

        // Filter by maximum amount
        if (criteria.getMaxAmount() != null) {
            builder.and(transaction.amount.loe(criteria.getMaxAmount()));
        }

        // Filter by created after date
        if (criteria.getCreatedAfter() != null) {
            builder.and(transaction.createdAt.goe(criteria.getCreatedAfter()));
        }

        // Filter by created before date
        if (criteria.getCreatedBefore() != null) {
            builder.and(transaction.createdAt.loe(criteria.getCreatedBefore()));
        }

        // Filter by processed after date
        if (criteria.getProcessedAfter() != null) {
            builder.and(transaction.processedAt.goe(criteria.getProcessedAfter()));
        }

        // Filter by processed before date
        if (criteria.getProcessedBefore() != null) {
            builder.and(transaction.processedAt.loe(criteria.getProcessedBefore()));
        }

        return builder;
    }
}

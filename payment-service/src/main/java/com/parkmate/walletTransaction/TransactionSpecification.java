package com.parkmate.walletTransaction;

import com.parkmate.walletTransaction.dto.TransactionSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class TransactionSpecification {

    public static Predicate buildPredicate(TransactionSearchCriteria criteria, Long userId) {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }

        // Filter by user ID from header (applies when ownedByMe is true)
        if (userId != null && Boolean.TRUE.equals(criteria.getOwnedByMe())) {
            builder.and(transaction.userId.eq(userId));
        }
        // Filter by specific user ID (only when ownedByMe is explicitly false - for admin use)
        else if (criteria.getUserId() != null && Boolean.FALSE.equals(criteria.getOwnedByMe())) {
            builder.and(transaction.userId.eq(criteria.getUserId()));
        }
        // Filter by user ID when ownedByMe is null
        else if (criteria.getUserId() != null && criteria.getOwnedByMe() == null) {
            builder.and(transaction.userId.eq(criteria.getUserId()));
        }
        // Default: when userId from header is present and ownedByMe is null, filter by header userId
        else if (userId != null && criteria.getOwnedByMe() == null && criteria.getUserId() == null) {
            builder.and(transaction.userId.eq(userId));
        }

        // Filter by transaction ID
        if (criteria.getId() != null) {
            builder.and(transaction.id.eq(criteria.getId()));
        }

        // Filter by wallet ID
        if (criteria.getWalletId() != null) {
            builder.and(transaction.walletId.eq(criteria.getWalletId()));
        }

        // Filter by session ID
        if (criteria.getSessionId() != null) {
            builder.and(transaction.sessionId.eq(criteria.getSessionId()));
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

    /**
     * Build predicate for user's own transactions
     *
     * @param userId User ID
     * @return Predicate filtering by user ID
     */
    public static Predicate forUser(Long userId) {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        return transaction.userId.eq(userId);
    }

    /**
     * Build predicate for successful transactions
     *
     * @return Predicate filtering by completed status
     */
    public static Predicate successfulOnly() {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        return transaction.status.eq(TransactionStatus.COMPLETED);
    }

    /**
     * Build predicate for pending transactions
     *
     * @return Predicate filtering by pending status
     */
    public static Predicate pendingOnly() {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        return transaction.status.eq(TransactionStatus.PENDING);
    }

    /**
     * Build predicate for transactions within date range
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Predicate filtering by date range
     */
    public static Predicate betweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        BooleanBuilder builder = new BooleanBuilder();

        if (startDate != null) {
            builder.and(transaction.createdAt.goe(startDate));
        }

        if (endDate != null) {
            builder.and(transaction.createdAt.loe(endDate));
        }

        return builder;
    }

    /**
     * Build predicate for transactions by type
     *
     * @param type Transaction type
     * @return Predicate filtering by transaction type
     */
    public static Predicate byType(TransactionType type) {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        return transaction.transactionType.eq(type);
    }
}

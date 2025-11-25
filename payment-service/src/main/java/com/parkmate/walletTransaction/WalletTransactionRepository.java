package com.parkmate.walletTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID>,
        QuerydslPredicateExecutor<WalletTransaction> {

    Optional<WalletTransaction> findByExternalTransactionId(String externalTransactionId);

    List<WalletTransaction> findByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);


    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN wt.transactionType = 'SUBSCRIPTION_PAYMENT' AND wt.status = 'COMPLETED' THEN wt.amount ELSE 0 END), 0.0) * :platformFee as totalSubscriptionPlatformFee, " +
            "COALESCE(SUM(CASE WHEN wt.transactionType = 'DEDUCTION' AND wt.status = 'COMPLETED' THEN wt.amount ELSE 0 END), 0.0) * :platformFee as totalSessionPlatformFee, " +
            "COALESCE(SUM(CASE WHEN wt.transactionType = 'RESERVATION_PAYMENT' AND wt.status = 'COMPLETED' THEN wt.amount ELSE 0 END), 0.0) * :platformFee as totalReservationPlatformFee " +
            "FROM WalletTransaction wt " +
            "WHERE wt.createdAt BETWEEN :from AND :to")
    PlatformRevenueProjection getPlatformRevenueStatistic(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("platformFee") BigDecimal platformFee
    );
}
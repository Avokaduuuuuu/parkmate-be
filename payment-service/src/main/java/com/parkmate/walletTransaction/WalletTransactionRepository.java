package com.parkmate.walletTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID>,
        QuerydslPredicateExecutor<WalletTransaction> {

    Optional<WalletTransaction> findByExternalTransactionId(String externalTransactionId);


}
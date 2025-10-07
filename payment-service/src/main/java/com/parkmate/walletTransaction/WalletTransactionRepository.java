package com.parkmate.walletTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    Optional<WalletTransaction> findBySessionId(UUID sessionId);

    Optional<WalletTransaction> findByExternalTransactionId(String externalTransactionId);

}
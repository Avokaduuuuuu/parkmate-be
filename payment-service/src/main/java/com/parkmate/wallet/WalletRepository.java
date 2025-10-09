package com.parkmate.wallet;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(@NonNull Long Long);

    boolean existsByUserId(@NonNull Long userId);

    Page<Wallet> findAllByUserId(@NonNull Long userId, Pageable pageable);

    @Query("SELECT w.balance FROM Wallet w WHERE w.userId = :userId")
    BigDecimal getBalanceByUserId(@NonNull Long userId);
}



package com.parkmate.walletTransaction;

import com.github.f4b6a3.uuid.UuidCreator;
import com.parkmate.wallet.Wallet;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transaction")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WalletTransaction {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "wallet_id", nullable = false)
    Long walletId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_wallet_transaction_wallet"))
    Wallet wallet;

    @Column(name = "session_id", nullable = false)
    UUID sessionId;

    @Column(name = "transaction_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @Column(name = "fee", nullable = false)
    BigDecimal fee;

    @Column(name = "net_amount")
    BigDecimal netAmount;

    @Column(name = "external_transaction_id", nullable = false, length = 3)
    String externalTransactionId;

    @Column(name = "gateway_response")
    @JdbcTypeCode(SqlTypes.JSON)
    String gatewayResponse;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    TransactionStatus status;

    @Column(name = "processed_at")
    @LastModifiedDate
    LocalDateTime processedAt;

    @Column(name = "description", length = 255)
    String description;

    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    String metadata;

    @CreationTimestamp
    LocalDateTime createdAt;
}

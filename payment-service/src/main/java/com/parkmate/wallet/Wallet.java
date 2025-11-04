package com.parkmate.wallet;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Wallet {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "holder_id", nullable = false, unique = true)
    Long holderId;

    @ColumnDefault("'MEMBER'")
    @Column(name = "wallet_owner", columnDefinition = "wallet_owner not null")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private WalletOwner walletOwner;

    @Column(name = "balance", nullable = false)
    BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    String currency;

    @Column(name = "is_active", nullable = false)
    boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

}

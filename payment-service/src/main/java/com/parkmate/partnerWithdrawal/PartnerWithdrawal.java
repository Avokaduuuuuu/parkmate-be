package com.parkmate.partnerWithdrawal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "partner_withdrawal")
public class PartnerWithdrawal {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @NotNull
    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "periods", nullable = false)
    private Map<String, Object> periods;

    @NotNull
    @Column(name = "total_gross_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGrossRevenue;

    @NotNull
    @Column(name = "total_platform_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPlatformFee;

    @NotNull
    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PROCESSING'")
    @Column(name = "status", columnDefinition = "withdrawal_status not null")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private WithdrawalStatus status;

    @Column(name = "failure_reason", length = Integer.MAX_VALUE)
    private String failureReason;

    @Size(max = 255)
    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response")
    private Map<String, Object> gatewayResponse;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ColumnDefault("0.00")
    @Column(name = "total_amount_reservation", precision = 15, scale = 2)
    private BigDecimal totalAmountReservation;

    @ColumnDefault("0.00")
    @Column(name = "total_amount_subscription", precision = 15, scale = 2)
    private BigDecimal totalAmountSubscription;

    @ColumnDefault("0.00")
    @Column(name = "total_amount_walk_in", precision = 15, scale = 2)
    private BigDecimal totalAmountWalkIn;

    @Column(name = "requested_at")
    @ColumnDefault("CURRENT_TIMESTAMP")
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}
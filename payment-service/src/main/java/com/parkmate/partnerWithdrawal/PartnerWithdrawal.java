package com.parkmate.partnerWithdrawal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @ColumnDefault("'PROCESSING'")
    @Column(name = "status", columnDefinition = "withdrawal_status not null")
    private Object status;

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


}
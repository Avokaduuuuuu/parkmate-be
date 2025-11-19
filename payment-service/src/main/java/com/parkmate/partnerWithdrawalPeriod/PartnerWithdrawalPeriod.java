package com.parkmate.partnerWithdrawalPeriod;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "partner_withdrawal_period")
public class PartnerWithdrawalPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @NotNull
    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @NotNull
    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @NotNull
    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Size(max = 50)
    @NotNull
    @Column(name = "period_label", nullable = false, length = 50)
    private String periodLabel;

    @ColumnDefault("false")
    @Column(name = "is_withdrawn")
    private Boolean isWithdrawn;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ColumnDefault("0.00")
    @Column(name = "reservation_revenue", precision = 15, scale = 2)
    private BigDecimal reservationRevenue;

    @ColumnDefault("0.00")
    @Column(name = "subscription_revenue", precision = 15, scale = 2)
    private BigDecimal subscriptionRevenue;

    @ColumnDefault("0.00")
    @Column(name = "walk_in_revenue", precision = 15, scale = 2)
    private BigDecimal walkInRevenue;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "gross_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossRevenue;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "platform_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal platformFee;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "net_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal netRevenue;

    @ColumnDefault("0")
    @Column(name = "total_sessions")
    private Integer totalSessions;

    @ColumnDefault("0")
    @Column(name = "walk_in_sessions")
    private Integer walkInSessions;

    @ColumnDefault("0")
    @Column(name = "reservation_sessions")
    private Integer reservationSessions;

    @ColumnDefault("0")
    @Column(name = "subscription_sessions")
    private Integer subscriptionSessions;

    @Column(name = "withdrawal_id")
    private java.util.UUID withdrawalId;

}
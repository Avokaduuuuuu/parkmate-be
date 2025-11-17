package com.parkmate.partnerWithdrawalPeriod;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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


}
package com.parkmate.operational_payment;

import com.parkmate.common.BaseEntity;
import com.parkmate.operational_payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lot_operational_payment")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OperationalPaymentEntity extends BaseEntity {
    @Column(name = "billing_start_date")
    LocalDate billingStartDate;

    @Column(name = "billing_end_date")
    LocalDate billingEndDate;

    @Column(name = "billing_period_months")
    Integer billingPeriodMonths;

    @Column(name = "lot_area_sqm")
    Double lotAreaSqm;

    @Column(name = "fee_per_sqm")
    BigDecimal feePerSqm;

    @Column(name = "total_fee")
    BigDecimal totalFee;

    @Column(name = "payment_transaction_id")
    String paymentTransactionId;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    PaymentStatus paymentStatus;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @Column(name = "due_date")
    LocalDate dueDate;

    @Column(name = "notes")
    String notes;
}

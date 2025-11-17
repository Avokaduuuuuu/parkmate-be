package com.parkmate.operational_payment;

import com.parkmate.operational_payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lot_operational_payment")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OperationalPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}

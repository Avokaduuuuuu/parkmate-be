package com.parkmate.operationalPayment;

import com.parkmate.device_payment_item.DevicePaymentItemEntity;
import com.parkmate.operationalPayment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lot_operational_payment")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OperationalPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "lot_id", nullable = false)
    Long lotId;

    @Column(name = "partner_id", nullable = false)
    Long partnerId;

    @Column(name = "fee_config_id", nullable = false)
    Long feeConfigId;

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

    @Column(name = "area_fee")
    BigDecimal areaFee;

    @Column(name = "device_fee")
    BigDecimal deviceFee;

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

    @OneToMany(mappedBy = "operationalPayment", cascade = CascadeType.ALL)
    List<DevicePaymentItemEntity> devicePaymentItems;
}

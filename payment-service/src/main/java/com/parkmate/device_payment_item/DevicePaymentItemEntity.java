package com.parkmate.device_payment_item;

import com.parkmate.common.enums.DeviceType;
import com.parkmate.device_fee_config.DeviceFeeConfigEntity;
import com.parkmate.operationalPayment.OperationalPaymentEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "device_payment_item")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevicePaymentItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    DeviceType deviceType;

    @Column(name = "total_device")
    Integer totalDevice;

    @Column(name = "device_fee")
    BigDecimal deviceFee;

    @Column(name = "total_fee")
    BigDecimal totalFee;

    @Column(name = "created_at")
    @CreatedDate
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "operational_payment_id")
    OperationalPaymentEntity operationalPayment;

    @ManyToOne
    @JoinColumn(name = "device_fee_config_id")
    DeviceFeeConfigEntity deviceFeeConfig;
}

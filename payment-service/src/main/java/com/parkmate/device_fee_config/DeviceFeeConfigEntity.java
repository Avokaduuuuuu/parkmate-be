package com.parkmate.device_fee_config;

import com.parkmate.common.enums.DeviceType;
import com.parkmate.device_payment_item.DevicePaymentItemEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "device_fee_config")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceFeeConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    DeviceType deviceType;

    @Column(name = "device_fee")
    BigDecimal deviceFee;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "valid_from")
    LocalDateTime validFrom;

    @Column(name = "valid_until")
    LocalDateTime validTo;

    @Column(name = "description")
    String description;

    @Column(name = "created_at")
    @CreatedDate
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "deviceFeeConfig", cascade = CascadeType.ALL)
    List<DevicePaymentItemEntity> devicePaymentItems;
}

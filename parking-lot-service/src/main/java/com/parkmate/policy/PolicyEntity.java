package com.parkmate.policy;

import com.parkmate.common.BaseEntity;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.policy.enums.PolicyType;
import com.parkmate.session.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "policy")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PolicyEntity extends BaseEntity {

    @Column(name = "policy_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    PolicyType policyType;

    @Column(name = "value")
    Integer value;

    @Column(name = "rate")
    Double rate;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "sync_status")
    @Enumerated(EnumType.STRING)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    SyncStatus syncStatus;

    @Column(name = "synced_at")
    LocalDateTime syncedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;
}

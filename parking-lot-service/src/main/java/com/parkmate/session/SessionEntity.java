package com.parkmate.session;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.util.UuidUtil;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.session.enums.AuthMethod;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.session.enums.SessionType;
import com.parkmate.session.enums.SyncStatus;
import com.parkmate.spot.SpotEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "session")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class SessionEntity {
    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "vehicle_id")
    Long vehicleId;

    @Column(name = "license_plate")
    String licensePlate;

    @Column(name = "session_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    SessionType sessionType;

    @Column(name = "auth_method")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    AuthMethod authMethod;

    @Column(name = "entry_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime entryTime;

    @Column(name = "exit_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime exitTime;

    @Column(name = "duration_minute")
    Integer durationMinute;

    @Column(name = "total_amount")
    BigDecimal totalAmount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    SessionStatus status;

    @Column(name = "sync_status")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    SyncStatus syncStatus;

    @Column(name = "synced_from_local")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime syncedFromLocal;

    @Column(name = "note")
    String note;

    @Column(name = "card_uuid")
    String cardUUID;

    @Column(name = "created_at")
    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    ParkingLotEntity parkingLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    SpotEntity spot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    PricingRuleEntity pricingRule;


    @PrePersist
    private void initUUID() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}

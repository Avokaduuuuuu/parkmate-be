package com.parkmate.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.user.User;
import com.parkmate.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;

    @Column(name = "parking_lot_id", nullable = false)
    Long parkingLotId;

    @Column(name = "pricing_rule_id", nullable = false)
    Long pricingRuleId;

    @Column(name = "reserved_from", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedFrom;

    @Column(name = "reserved_until")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime reservedUntil;

    @Column(name = "assumed_stay_minute")
    Integer assumedStayMinute;

    @Column(name = "initial_fee", precision = 10, scale = 2)
    BigDecimal initialFee;

    @Column(name = "total_fee", precision = 10, scale = 2)
    BigDecimal totalFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    @Column(name = "session_id")
    private UUID sessionId;

    @ColumnDefault("false")
    @Column(name = "is_used")
    private Boolean isUsed;


}

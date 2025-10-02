package com.parkmate.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_user"))
    private User user;

    @Column(name = "parking_lot_id", nullable = false)
    private Long parkingLotId;

    @Column(name = "lot_id", nullable = false)
    private String lotId;

    @Column(name = "reserved_from", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservedFrom;

    @Column(name = "reserved_until", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservedUntil;

    @Column(name = "reservation_fee", precision = 10, scale = 2)
    private Long reservationFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE || status == ReservationStatus.PENDING;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(reservedUntil);
    }

    public boolean isStarted() {
        return LocalDateTime.now().isAfter(reservedFrom);
    }

    public void confirm() {
        this.status = ReservationStatus.ACTIVE;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }
}

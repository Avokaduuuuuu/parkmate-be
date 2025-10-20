package com.parkmate.reservation;

import com.parkmate.reservation.dto.ReservationSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class ReservationSpecification {

    public static Predicate buildPredicate(ReservationSearchCriteria criteria) {
        QReservation reservation = QReservation.reservation;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }

        // Filter by reservation ID
        if (criteria.getId() != null) {
            builder.and(reservation.id.eq(criteria.getId()));
        }

        // Filter by user ID
        if (criteria.getUserId() != null) {
            builder.and(reservation.userId.eq(criteria.getUserId()));
        }

        // Filter by vehicle ID
        if (criteria.getVehicleId() != null) {
            builder.and(reservation.vehicleId.eq(criteria.getVehicleId()));
        }

        // Filter by parking lot ID
        if (criteria.getParkingLotId() != null) {
            builder.and(reservation.parkingLotId.eq(criteria.getParkingLotId()));
        }

        // Filter by spot ID
        if (criteria.getSpotId() != null) {
            builder.and(reservation.spotId.eq(criteria.getSpotId()));
        }

        // Filter by status
        if (criteria.getStatus() != null) {
            builder.and(reservation.status.eq(criteria.getStatus()));
        }

        // Filter by reserved from date (exact match)
        if (criteria.getStartDate() != null) {
            builder.and(reservation.reservedFrom.eq(criteria.getStartDate()));
        }

        // Filter by created after date
        if (criteria.getCreatedAfter() != null) {
            builder.and(reservation.createdAt.goe(criteria.getCreatedAfter()));
        }

        // Filter by created before date
        if (criteria.getCreatedBefore() != null) {
            builder.and(reservation.createdAt.loe(criteria.getCreatedBefore()));
        }

        return builder;
    }

    /**
     * Build predicate for user's own reservations
     *
     * @param userId User ID
     * @return Predicate filtering by user ID
     */
    public static Predicate forUer(Long userId) {
        QReservation reservation = QReservation.reservation;
        return reservation.userId.eq(userId);
    }

    /**
     * Build predicate for active reservations (not cancelled)
     *
     * @return Predicate filtering out cancelled reservations
     */
    public static Predicate activeOnly() {
        QReservation reservation = QReservation.reservation;
        return reservation.status.ne(com.parkmate.common.enums.ReservationStatus.CANCELLED);
    }

    /**
     * Build predicate for reservations within date range
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Predicate filtering by date range
     */
    public static Predicate betweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        QReservation reservation = QReservation.reservation;
        BooleanBuilder builder = new BooleanBuilder();

        if (startDate != null) {
            builder.and(reservation.reservedFrom.goe(startDate));
        }

        if (endDate != null) {
            builder.and(reservation.reservedFrom.loe(endDate));
        }

        return builder;
    }
}

package com.parkmate.reservation;

import com.parkmate.common.enums.ReservationStatus;
import com.querydsl.core.types.Predicate;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, QuerydslPredicateExecutor<Reservation> {
    @Override
    Page<Reservation> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable);


    @Override
    Optional<Reservation> findOne(@NonNull Predicate predicate);

    @Override
    long count(@NonNull Predicate predicate);

    List<Reservation> findAllByParkingLotIdAndStatus(Long parkingLotId, ReservationStatus status);

    List<Reservation> findAllByVehicleIdInAndStatusIn(List<Long> vehicleIds, List<ReservationStatus> status);

    @Query(value = """
            SELECT COUNT(*) FROM reservation r INNER JOIN vehicle v ON r.vehicle_id = v.id
            WHERE r.parking_lot_id = :parkingLotId
            AND r.reserved_from < :to
            AND (r.reserved_from + (r.assumed_stay_minute * INTERVAL '1 minute')) > :from
            AND r.status IN ('ACTIVE', 'PENDING')
            AND v.vehicle_type = CAST(:vehicleType AS vehicle_type)
            """,
            nativeQuery = true)
    Long findOverlapReservations(
            @Param("parkingLotId") Long parkingLotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("vehicleType") String vehicleType
    );

    @Query("SELECT " +
            "COUNT(CASE WHEN r.status = 'ACTIVE' THEN 1 END) AS activeCount, " +
            "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) AS completedCount, " +
            "COUNT(CASE WHEN r.status = 'PENDING' THEN 1 END) AS pendingCount, " +
            "COUNT(CASE WHEN r.status = 'CANCELLED' THEN 1 END) AS cancelledCount, " +
            "COUNT(CASE WHEN r.status = 'EXPIRED' THEN 1 END) AS expiredCount, " +
            "COALESCE(SUM(CASE WHEN r.status IN ('COMPLETED', 'CANCELLED', 'EXPIRED') THEN r.totalFee ELSE 0 END), 0) AS totalRevenue " +
            "FROM Reservation r " +
            "WHERE r.parkingLotId = :lotId AND r.reservedFrom >= :from AND (r.reservedUntil <= :to OR r.reservedUntil IS NULL)"
    )
    ReservationProjection getStatistic(@Param("lotId") Long lotId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(r.totalFee), 0) " +
            "FROM Reservation r " +
            "WHERE r.parkingLotId = :lotId " +
            "AND r.status IN ('CANCELLED', 'EXPIRED') " +
            "AND r.createdAt >= :from " +
            "AND r.createdAt <= :to")
    java.math.BigDecimal getTotalPenaltyRevenue(
            @Param("lotId") Long lotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.parkingLotId = :lotId " +
            "AND r.status IN ('CANCELLED', 'EXPIRED') " +
            "AND r.createdAt >= :from " +
            "AND r.createdAt <= :to")
    Long getTotalPenaltyCount(
            @Param("lotId") Long lotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<Reservation> findByStatusAndReservedFromBetween(
            ReservationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Reservation> findByParkingLotIdAndStatus(Long parkingLotId, ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.status IN :statuses")
    long countByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<ReservationStatus> statuses);

    boolean existsByVehicleIdAndStatusIn(Long vehicleId, List<ReservationStatus> statuses);

    boolean existsByVehicleIdAndSessionIdIsNotNullAndStatusIn(Long vehicleId, List<ReservationStatus> statuses);
}

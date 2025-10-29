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

    @Query("SELECT DISTINCT r.spotId FROM Reservation r " +
            "WHERE r.spotId IN :spotIds " +
            "AND r.reservedFrom < :to " +
            "AND r.reservedUntil > :from " +
            "AND r.status != 'CANCELLED'" +
            "AND r.status != 'COMPLETED'" +
            "AND r.status != 'EXPIRED'")
    List<Long> findOccupiedSpotIds(
            @Param("spotIds") List<Long> spotIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

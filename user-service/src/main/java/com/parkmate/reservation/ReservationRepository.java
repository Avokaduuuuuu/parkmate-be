package com.parkmate.reservation;

import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.vehicle.VehicleType;
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
            SELECT COUNT(*) FROM reservation r  INNER JOIN vehicle v ON r.vehicle_id = v.id
            WHERE r.parking_lot_id = :parkingLotId
            AND (r.reserved_from - (r.assumed_stay_minute * INTERVAL '1 minute')) < :to 
            AND (r.reserved_from + (r.assumed_stay_minute * INTERVAL '1 minute')) > :from 
            AND r.status IN ('ACTIVE', 'PENDING')
            AND v.vehicle_type =  CAST(:vehicleType AS vehicle_type)
            """,
            nativeQuery = true)
    Long findOverlapReservations(
            @Param("parkingLotId") Long parkingLotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("vehicleType") String vehicleType
    );
}

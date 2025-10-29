package com.parkmate.reservation;

import com.parkmate.common.enums.ReservationStatus;
import com.querydsl.core.types.Predicate;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

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
}

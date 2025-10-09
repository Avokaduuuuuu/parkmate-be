package com.parkmate.reservation;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, QuerydslPredicateExecutor<Reservation> {

    /**
     * Find reservations by predicate with pagination
     * Inherited from QuerydslPredicateExecutor
     */
    @Override
    Page<Reservation> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find single reservation by predicate
     * Inherited from QuerydslPredicateExecutor
     */
    @Override
    Optional<Reservation> findOne(Predicate predicate);

    /**
     * Count reservations matching predicate
     * Inherited from QuerydslPredicateExecutor
     */
    @Override
    long count(Predicate predicate);
}

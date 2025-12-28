package com.parkmate.vehicle;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, QuerydslPredicateExecutor<Vehicle> {

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIsActiveTrue(String licensePlate);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    Page<Vehicle> findAll(Predicate predicate, Pageable pageable);

    List<Vehicle> findAllByUserIdAndIsActiveIsTrue(Long userId);

}


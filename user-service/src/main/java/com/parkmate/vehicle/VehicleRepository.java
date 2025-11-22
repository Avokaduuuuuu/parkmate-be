package com.parkmate.vehicle;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, QuerydslPredicateExecutor<Vehicle> {

    boolean existsByLicensePlate(String licensePlate);

    Page<Vehicle> findAll(Predicate predicate, Pageable pageable);

    List<Vehicle> findAllByUserIdAndIsActiveIsTrue(Long userId);

}


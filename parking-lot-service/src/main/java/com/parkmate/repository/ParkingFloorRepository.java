package com.parkmate.repository;

import com.parkmate.entity.ParkingFloorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingFloorRepository extends JpaRepository<ParkingFloorEntity, Long> {
}

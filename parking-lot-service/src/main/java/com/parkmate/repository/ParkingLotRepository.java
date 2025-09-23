package com.parkmate.repository;

import com.parkmate.entity.ParkingLotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLotEntity, Long> {
}

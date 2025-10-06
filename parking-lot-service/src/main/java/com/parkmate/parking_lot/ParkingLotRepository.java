package com.parkmate.parking_lot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ParkingLotRepository extends JpaRepository<ParkingLotEntity, Long>, JpaSpecificationExecutor<ParkingLotEntity> {
}

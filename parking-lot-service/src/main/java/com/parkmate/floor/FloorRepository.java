package com.parkmate.floor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FloorRepository extends JpaRepository<FloorEntity, Long>, JpaSpecificationExecutor<FloorEntity> {
    Long countAllBy();

    List<FloorEntity> findByParkingLot_IdAndIsActive(Long parkingLotId, Boolean isActive);
}

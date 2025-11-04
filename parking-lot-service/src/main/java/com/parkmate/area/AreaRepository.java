package com.parkmate.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

public interface AreaRepository extends JpaRepository<AreaEntity, Long>, JpaSpecificationExecutor<AreaEntity> {
    Long countAllBy();

    @Query("SELECT SUM(a.totalSpots) FROM AreaEntity a " +
            "WHERE a.parkingFloor.parkingLot.id = :parkingLotId " +
            "AND a.areaType = 'EMERGENCY_ONLY'")
    Long countEmergencySpot(@Param("parkingLotId") Long parkingLotId);
}

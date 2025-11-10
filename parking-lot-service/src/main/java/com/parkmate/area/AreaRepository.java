package com.parkmate.area;

import com.parkmate.area.enums.AreaType;
import com.parkmate.common.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AreaRepository extends JpaRepository<AreaEntity, Long>, JpaSpecificationExecutor<AreaEntity> {
    Long countAllBy();

    @Query("SELECT SUM(a.totalSpots) FROM AreaEntity a " +
            "WHERE a.parkingFloor.parkingLot.id = :parkingLotId " +
            "AND a.areaType = 'EMERGENCY_ONLY'")
    Long countEmergencySpot(@Param("parkingLotId") Long parkingLotId);

    List<AreaEntity> findByParkingFloor_IdAndVehicleTypeAndAreaTypeAndIsActive(
            Long floorId, VehicleType vehicleType, AreaType areaType, Boolean isActive
    );

    @Query("SELECT SUM(a.totalSpots) FROM AreaEntity a " +
            "WHERE a.parkingFloor.id = :floorId " +
            "AND a.vehicleType = :vehicleType " +
            "AND a.areaType =  'SUBSCRIPTION_ONLY' " +
            "AND a.isActive = true")
    Integer countTotalSubscriptionSpots(
            @Param("floorId") Long floorId,
            @Param("vehicleType") VehicleType vehicleType
    );

    List<AreaEntity> findByParkingFloor_IdAndAreaTypeAndIsActive(Long floorId, AreaType areaType, boolean isActive);
}

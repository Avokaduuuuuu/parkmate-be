package com.parkmate.spot;

import com.parkmate.common.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpotRepository extends JpaRepository<SpotEntity, Long>, JpaSpecificationExecutor<SpotEntity> {
    List<SpotEntity> findByParkingArea_Id(Long parkingAreaId);

    @Query("SELECT s.id FROM SpotEntity s WHERE s.parkingArea.id = :areaId")
    List<Long> findIdsByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT s.id FROM SpotEntity s " +
            "WHERE s.parkingArea.parkingFloor.id = :floorId " +
            "AND s.parkingArea.vehicleType = :vehicleType " +
            "AND s.parkingArea.areaType = 'SUBSCRIPTION_ONLY' " +
            "AND s.parkingArea.isActive = true")
    List<Long> findIdsByFloorAndVehicleTypeAndSubscriptionOnly(
            @Param("floorId") Long floorId,
            @Param("vehicleType") VehicleType vehicleType
    );
}

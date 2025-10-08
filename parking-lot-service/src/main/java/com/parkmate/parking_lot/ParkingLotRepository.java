package com.parkmate.parking_lot;

import com.parkmate.parking_lot.enums.ParkingLotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLotEntity, Long>,
        JpaSpecificationExecutor<ParkingLotEntity> {

    List<ParkingLotEntity> findByPartnerId(Long partnerId);

    List<ParkingLotEntity> findByStatus(ParkingLotStatus status);
}
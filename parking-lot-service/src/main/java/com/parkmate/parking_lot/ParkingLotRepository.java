package com.parkmate.parking_lot;

import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.rating.RatingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLotEntity, Long>,
        JpaSpecificationExecutor<ParkingLotEntity> {

    List<ParkingLotEntity> findByPartnerId(Long partnerId);

    List<ParkingLotEntity> findByStatus(ParkingLotStatus status);


    @Query("SELECT " +
            "COALESCE(COUNT(p), 0) AS total," +
            "COALESCE(COUNT(CASE WHEN p.status = 'ACTIVE' THEN 1 END), 0) AS activeTotal," +
            "COALESCE(COUNT(CASE WHEN p.status = 'PENDING' THEN 1 END), 0) AS pendingTotal," +
            "COALESCE(COUNT(CASE WHEN p.status = 'UNDER_MAINTENANCE' THEN 1 END), 0) AS underMaintenanceTotal," +
            "COALESCE(COUNT(CASE WHEN p.status = 'PREPARING' THEN 1 END), 0) AS preparingTotal " +
            "FROM ParkingLotEntity p")
    PlatformLotProjection getPlatformLotStatistic();

    @Query("SELECT r " +
            "FROM RatingEntity r " +
            "WHERE r.parkingLot.id = :parkingLotId " +
            "ORDER BY r.overallRating DESC ")
    List<RatingEntity> getTop3RatingsByParkingLotId(Long parkingLotId, Pageable pageable);

    @Query("SELECT p.partnerId as partnerId, COUNT(p.id) as total " +
            "FROM ParkingLotEntity p " +
            "WHERE p.partnerId IN :partnerIds " +
            "GROUP BY p.partnerId")
    List<ParkingLotCountProjection> countByPartnerIds(@Param("partnerIds") List<Long> partnerIds);
}
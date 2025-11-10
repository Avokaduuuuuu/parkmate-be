package com.parkmate.session;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.statistic.ParkingLotStatisticProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID>, JpaSpecificationExecutor<SessionEntity> {
    Optional<SessionEntity> findByCardUUIDAndStatus(String cardUUID, SessionStatus status);

    Optional<SessionEntity> findByCardUUID(String cardUUID);

    @Query("SELECT COUNT(s) FROM SessionEntity s " +
            "WHERE s.parkingLot.id = :parkingLotId " +
            "AND s.vehicleType = :vehicleType " +
            "AND s.status = 'ACTIVE' " +
            "AND s.referenceType = 'WALK_IN' " +
            "AND s.entryTime >= :entryTimeFrom")
    Integer countActiveWalkInsSince(
            @Param("parkingLotId") Long parkingLotId,
            @Param("entryTimeFrom") LocalDateTime entryTimeFrom,
            @Param("vehicleType") VehicleType vehicleType
    );


    @Query("SELECT  " +
            "SUM(CASE WHEN s.status IN ('COMPLETED', 'MANUAL_COMPLETED') THEN s.totalAmount ELSE 0.0 END) AS total, " +
            "COUNT(CASE WHEN s.status IN ('COMPLETED', 'MANUAL_COMPLETED') THEN 1 END) AS completedCount, " +
            "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) AS activeCount " +
            "FROM SessionEntity s " +
            "WHERE s.parkingLot.id = :lotId AND s.entryTime >= :from AND (s.exitTime <= :to OR s.exitTime IS NULL)")
    ParkingLotStatisticProjection getParkingLotStatistic(
      @Param("lotId") Long lotId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to
    );
}

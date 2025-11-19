package com.parkmate.session;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.ReferenceType;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.session.enums.SessionType;
import com.parkmate.statistic.ParkingLotStatisticProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
            "SUM(CASE WHEN s.status IN ('COMPLETED', 'MANUAL_COMPLETED') AND s.sessionType = 'MEMBER' THEN s.totalAmount ELSE 0.0 END) AS memberTotal, " +
            "SUM(CASE WHEN s.status IN ('COMPLETED', 'MANUAL_COMPLETED') AND s.sessionType = 'OCCASIONAL' THEN s.totalAmount ELSE 0.0 END) AS occasionalTotal, " +
            "COUNT(CASE WHEN s.status IN ('COMPLETED', 'MANUAL_COMPLETED') THEN 1 END) AS completedCount, " +
            "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END) AS activeCount, " +
            "COUNT(CASE WHEN s.vehicleType = 'MOTORBIKE' THEN 1 END) AS motorbikeCount, " +
            "COUNT(CASE WHEN s.vehicleType = 'CAR_UP_TO_9_SEATS' THEN 1 END) AS carCount, " +
            "COUNT(CASE WHEN s.vehicleType = 'BIKE' THEN 1 END) AS bikeCount, " +
            "COUNT(CASE WHEN s.vehicleType = 'OTHER' THEN 1 END) AS otherCount, " +
            "COALESCE(AVG(CASE WHEN s.durationMinute IS NOT NULL THEN s.durationMinute END), 0.0) AS averageDurationMinute, " +
            "COUNT(CASE WHEN s.sessionType = 'MEMBER' THEN 1 END) AS memberCount, " +
            "COUNT(CASE WHEN s.sessionType = 'OCCASIONAL' THEN 1 END) AS occasionalCount " +
            "FROM SessionEntity s " +
            "WHERE s.parkingLot.id = :lotId AND s.entryTime >= :from AND (s.exitTime <= :to OR s.exitTime IS NULL)")
    ParkingLotStatisticProjection getParkingLotStatistic(
            @Param("lotId") Long lotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Get total revenue for sessions by session type and reference type
     * Used for calculating partner withdrawal periods
     *
     * @param lotId The parking lot ID
     * @param sessionType The session type (MEMBER or OCCASIONAL)
     * @param referenceType The reference type (WALK_IN, RESERVATION, or SUBSCRIPTION)
     * @param from Start date/time
     * @param to End date/time
     * @return Total revenue amount (returns 0 if no sessions found)
     */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM SessionEntity s " +
           "WHERE s.parkingLot.id = :lotId " +
           "AND s.sessionType = :sessionType " +
           "AND s.referenceType = :referenceType " +
           "AND s.status IN ('COMPLETED', 'MANUAL_COMPLETED') " +
           "AND s.entryTime >= :from " +
           "AND (s.exitTime <= :to OR s.exitTime IS NULL)")
    BigDecimal getRevenueByTypeAndReference(
            @Param("lotId") Long lotId,
            @Param("sessionType") SessionType sessionType,
            @Param("referenceType") ReferenceType referenceType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Get session count for sessions by session type and reference type
     * Used for calculating partner withdrawal periods
     *
     * @param lotId The parking lot ID
     * @param sessionType The session type (MEMBER or OCCASIONAL)
     * @param referenceType The reference type (WALK_IN, RESERVATION, or SUBSCRIPTION)
     * @param from Start date/time
     * @param to End date/time
     * @return Total session count
     */
    @Query("SELECT COUNT(s) FROM SessionEntity s " +
           "WHERE s.parkingLot.id = :lotId " +
           "AND s.sessionType = :sessionType " +
           "AND s.referenceType = :referenceType " +
           "AND s.status IN ('COMPLETED', 'MANUAL_COMPLETED') " +
           "AND s.entryTime >= :from " +
           "AND (s.exitTime <= :to OR s.exitTime IS NULL)")
    Long getCountByTypeAndReference(
            @Param("lotId") Long lotId,
            @Param("sessionType") SessionType sessionType,
            @Param("referenceType") ReferenceType referenceType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

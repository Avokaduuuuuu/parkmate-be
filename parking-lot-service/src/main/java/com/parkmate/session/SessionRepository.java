package com.parkmate.session;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.session.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long>, JpaSpecificationExecutor<SessionEntity> {
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
}

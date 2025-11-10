package com.parkmate.userSubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long>, QuerydslPredicateExecutor<UserSubscription> {

    List<UserSubscription> findByVehicleIdInAndStatusAndParkingLotId(List<Long> vehicleIds,
                                                                     UserSubscriptionStatus status,
                                                                     Long parkingLotId);

    @Query("SELECT DISTINCT uS.assignedSpotId FROM UserSubscription uS " +
            "WHERE uS.assignedSpotId IN :spotIds " +
            "AND uS.startDate < :to " +
            "AND uS.endDate > :from " +
            "AND uS.status IN ('ACTIVE','PENDING_PAYMENT')")
    List<Long> findOccupiedSpotIds(
            @Param("spotIds") List<Long> spotIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    boolean existsByUserIdAndParkingLotIdAndUserIdAndStatus(Long userId, Long parkingLotId, Long userId1, UserSubscriptionStatus status);

    List<UserSubscription> findByParkingLotIdAndSyncStatus(Long parkingLotId, SyncStatus syncStatus);
}

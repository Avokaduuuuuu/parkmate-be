package com.parkmate.userSubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long>, QuerydslPredicateExecutor<UserSubscription> {

    List<UserSubscription> findByVehicleIdInAndStatusInAndParkingLotId(List<Long> vehicleIds,
                                                                       List<UserSubscriptionStatus> status,
                                                                       Long parkingLotId);

    @Query("SELECT DISTINCT uS.assignedSpotId FROM UserSubscription uS " +
            "WHERE uS.assignedSpotId IN :spotIds " +
            "AND uS.startDate < :to " +
            "AND uS.endDate > :from " +
            "AND uS.status IN ('ACTIVE','PENDING_PAYMENT', 'INACTIVE')")
    List<Long> findOccupiedSpotIds(
            @Param("spotIds") List<Long> spotIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    boolean existsByUserIdAndParkingLotIdAndVehicleIdAndStatusIn(Long userId, Long parkingLotId, Long vehicleId, Collection<UserSubscriptionStatus> statuses);

    List<UserSubscription> findByParkingLotIdAndSyncStatus(Long parkingLotId, SyncStatus syncStatus);

    @Query("SELECT " +
            "SUM(us.paidAmount) " +
            "FROM UserSubscription us " +
            "WHERE us.parkingLotId = :lotId AND us.paidAt >= :from AND us.paidAt <= :to AND us.paidAt IS NOT NULL"
    )
    BigDecimal getTotalRevenue(
            @Param("lotId") Long lotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT " +
            "COUNT(us) " +
            "FROM UserSubscription us " +
            "WHERE us.parkingLotId = :lotId AND us.paidAt >= :from AND us.paidAt <= :to AND us.paidAt IS NOT NULL"
    )
    Long getTotalCount(
            @Param("lotId") Long lotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT " +
            "us.subscriptionPackageId as id, " +
            "COUNT(us.subscriptionPackageId) AS total," +
            "SUM(us.paidAmount) AS totalAmount " +
            "FROM UserSubscription us " +
            "WHERE us.parkingLotId = :lotId AND us.paidAt >= :from AND us.paidAt <= :to " +
            "GROUP BY us.subscriptionPackageId"
    )
    List<UserSubscriptionProjection> getUserSubscriptionStatistic(
            @Param("lotId") Long lotId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Find subscriptions by status list within an endDate range (for expiration reminders)
     *
     * @param statuses List of statuses to filter by (e.g., ACTIVE, INACTIVE)
     * @param start Start of the time range for endDate
     * @param end End of the time range for endDate
     * @return List of subscriptions matching the criteria
     */
    List<UserSubscription> findByStatusInAndEndDateBetween(
            List<UserSubscriptionStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Find subscriptions by status list, autoRenew flag, and endDate before a certain time (for auto-renewal processing)
     *
     * @param statuses List of statuses to filter by (e.g., ACTIVE, INACTIVE)
     * @param autoRenew The autoRenew flag value
     * @param endDate Find subscriptions with endDate before or equal to this time
     * @return List of subscriptions matching the criteria
     */
    List<UserSubscription> findByStatusInAndAutoRenewAndEndDateBefore(
            List<UserSubscriptionStatus> statuses,
            Boolean autoRenew,
            LocalDateTime endDate
    );

    /**
     * Find subscriptions by status list, autoRenew flag, and endDate within a time range (for daily balance checks)
     *
     * @param statuses List of statuses to filter by (e.g., ACTIVE, INACTIVE)
     * @param autoRenew The autoRenew flag value
     * @param start Start of the time range for endDate
     * @param end End of the time range for endDate
     * @return List of subscriptions matching the criteria
     */
    List<UserSubscription> findByStatusInAndAutoRenewAndEndDateBetween(
            List<UserSubscriptionStatus> statuses,
            Boolean autoRenew,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByVehicleIdAndStatusIn(Long vehicleId, Collection<UserSubscriptionStatus> statuses);
}

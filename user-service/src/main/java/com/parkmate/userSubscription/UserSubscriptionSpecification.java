package com.parkmate.userSubscription;

import com.parkmate.userSubscription.dto.UserSubscriptionSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.time.LocalDateTime;

public class UserSubscriptionSpecification {

    public static Predicate buildPredicate(UserSubscriptionSearchCriteria criteria) {

        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }

        // Filter by user ID from header or criteria
        if (criteria.getUserId() != null) {
            builder.and(userSubscription.user.id.eq(criteria.getUserId()));
        }

        // Filter by vehicle ID
        if (criteria.getVehicleId() != null) {
            builder.and(userSubscription.vehicle.id.eq(criteria.getVehicleId()));
        }

        // Filter by subscription package ID
        if (criteria.getSubscriptionPackageId() != null) {
            builder.and(userSubscription.subscriptionPackageId.eq(criteria.getSubscriptionPackageId()));
        }

        // Filter by parking lot ID
        if (criteria.getParkingLotId() != null) {
            builder.and(userSubscription.parkingLotId.eq(criteria.getParkingLotId()));
        }

        // Filter by assigned spot ID
        if (criteria.getAssignedSpotId() != null) {
            builder.and(userSubscription.assignedSpotId.eq(criteria.getAssignedSpotId()));
        }

        // Filter by status
        if (criteria.getStatus() != null) {
            builder.and(userSubscription.status.eq(criteria.getStatus()));
        }

        // Filter by auto-renew setting
        if (criteria.getAutoRenew() != null) {
            builder.and(userSubscription.autoRenew.eq(criteria.getAutoRenew()));
        }

        // Filter by start date range
        if (criteria.getStartDateFrom() != null) {
            builder.and(userSubscription.startDate.goe(criteria.getStartDateFrom()));
        }

        if (criteria.getStartDateTo() != null) {
            builder.and(userSubscription.startDate.loe(criteria.getStartDateTo()));
        }

        // Filter by end date range
        if (criteria.getEndDateFrom() != null) {
            builder.and(userSubscription.endDate.goe(criteria.getEndDateFrom()));
        }

        if (criteria.getEndDateTo() != null) {
            builder.and(userSubscription.endDate.loe(criteria.getEndDateTo()));
        }

        // Filter by active subscriptions (current date between start and end date)
        if (Boolean.TRUE.equals(criteria.getIsActive())) {
            LocalDateTime now = LocalDateTime.now().plusHours(7);
            builder.and(userSubscription.startDate.loe(now))
                    .and(userSubscription.endDate.goe(now))
                    .and(userSubscription.status.eq(UserSubscriptionStatus.ACTIVE));
        }

        // Filter by expired subscriptions (current date after end date)
        if (Boolean.TRUE.equals(criteria.getIsExpired())) {
            LocalDateTime now = LocalDateTime.now().plusHours(7);
            builder.and(userSubscription.endDate.lt(now));
        }

        return builder;
    }

    /**
     * Build predicate for user's own subscriptions
     *
     * @param userId User ID
     * @return Predicate filtering by user ID
     */
    public static Predicate forUser(Long userId) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        return userSubscription.user.id.eq(userId);
    }

    /**
     * Build predicate for active subscriptions
     *
     * @return Predicate filtering active subscriptions
     */
    public static Predicate activeOnly() {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        LocalDateTime now = LocalDateTime.now().plusHours(7);
        return userSubscription.startDate.loe(now)
                .and(userSubscription.endDate.goe(now))
                .and(userSubscription.status.eq(UserSubscriptionStatus.ACTIVE));
    }

    /**
     * Build predicate for subscriptions by parking lot
     *
     * @param parkingLotId Parking lot ID
     * @return Predicate filtering by parking lot ID
     */
    public static Predicate forParkingLot(Long parkingLotId) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        return userSubscription.parkingLotId.eq(parkingLotId);
    }

    /**
     * Build predicate for subscriptions by vehicle
     *
     * @param vehicleId Vehicle ID
     * @return Predicate filtering by vehicle ID
     */
    public static Predicate forVehicle(Long vehicleId) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        return userSubscription.vehicle.id.eq(vehicleId);
    }

    /**
     * Build predicate for subscriptions by status
     *
     * @param status Subscription status
     * @return Predicate filtering by status
     */
    public static Predicate withStatus(UserSubscriptionStatus status) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        return userSubscription.status.eq(status);
    }

    /**
     * Build predicate for subscriptions within date range
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Predicate filtering by date range
     */
    public static Predicate betweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        BooleanBuilder builder = new BooleanBuilder();

        if (startDate != null) {
            builder.and(userSubscription.startDate.goe(startDate));
        }

        if (endDate != null) {
            builder.and(userSubscription.endDate.loe(endDate));
        }

        return builder;
    }

    /**
     * Build predicate for subscriptions that overlap with a given date range
     * Used to check for conflicts when creating new subscriptions
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Predicate filtering overlapping subscriptions
     */
    public static Predicate overlappingWith(LocalDateTime startDate, LocalDateTime endDate) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        BooleanBuilder builder = new BooleanBuilder();

        // Subscription overlaps if:
        // (startDate <= subscription.endDate) AND (endDate >= subscription.startDate)
        if (startDate != null && endDate != null) {
            builder.and(userSubscription.endDate.goe(startDate))
                    .and(userSubscription.startDate.loe(endDate));
        }

        return builder;
    }

    /**
     * Build predicate for expiring subscriptions (ending within the next N days)
     *
     * @param days Number of days
     * @return Predicate filtering expiring subscriptions
     */
    public static Predicate expiringInDays(int days) {
        QUserSubscription userSubscription = QUserSubscription.userSubscription;
        LocalDateTime now = LocalDateTime.now().plusHours(7);
        LocalDateTime futureDate = now.plusDays(days);

        return userSubscription.endDate.between(now, futureDate)
                .and(userSubscription.status.eq(UserSubscriptionStatus.ACTIVE));
    }

}

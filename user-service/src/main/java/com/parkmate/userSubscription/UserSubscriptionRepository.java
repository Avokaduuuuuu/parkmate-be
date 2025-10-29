package com.parkmate.userSubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long>, QuerydslPredicateExecutor<UserSubscription> {

    List<UserSubscription> findByVehicleIdInAndStatusAndParkingLotId(List<Long> vehicleIds,
                                                                     UserSubscriptionStatus status,
                                                                     Long parkingLotId);
}

package com.parkmate.userSubscription;

import com.parkmate.userSubscription.dto.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSubscriptionService {

    UserSubscriptionResponse create(CreateUserSubscriptionRequest request, String accountIdHeader);

    UserSubscriptionResponse findById(Long id);

    Page<UserSubscriptionResponse> findAll(int page, int size, String sortBy, String sortOrder, String accountIdHeader, UserSubscriptionSearchCriteria searchCriteria);

    UserSubscriptionResponse update(Long id, UpdateUserSubscriptionRequest request);

    void delete(Long id);

    List<UserSubscriptionSyncResponse> getUserSubscriptionSync(Long lotId);

    void syncUserSubscription(Long id);

    List<Long> findOccupiedSpots(List<Long> parkingLotId, LocalDateTime startTime, LocalDateTime endTime);

    List<?> getFloorAvailability(Long parkingLotId, Long vehicleId, Long subscriptionPackageId, LocalDateTime startDate);

    List<?> getAreaAvailability(Long floorId, Long vehicleId, Long subscriptionPackageId, LocalDateTime startDate);

    List<?> getSpotAvailability(Long areaId, Long subscriptionPackageId, LocalDateTime startDate);

    Boolean holdSpot(Long userId, Long spotId);

    Object releaseSpot(Long spotId, String userIdHeader);
}

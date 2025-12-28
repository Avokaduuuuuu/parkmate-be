package com.parkmate.userSubscription;

import com.parkmate.userSubscription.dto.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
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

    BigDecimal getTotalRevenue(Long lotId,
                               LocalDateTime fromDate,
                               LocalDateTime toDate);

    Long getTotalCount(Long lotId,
                      LocalDateTime fromDate,
                      LocalDateTime toDate);

    UserSubscriptionResponse setRenewalDecision(Long subscriptionId, Boolean continueRenewal);

    /**
     * Cancel a user subscription with refund eligibility check
     * Refund is not given if:
     * - Subscription has been used (has parking session)
     * - More than half of the subscription period has passed
     *
     * @param subscriptionId The subscription ID to cancel
     * @param request        The cancellation request with reason
     * @param userIdHeader   The user ID from header for authorization
     * @return The cancelled subscription response
     */
    UserSubscriptionResponse cancelSubscription(Long subscriptionId, CancelSubscriptionRequest request, String userIdHeader);
}

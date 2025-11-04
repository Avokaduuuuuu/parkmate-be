package com.parkmate.reservation;

import com.parkmate.reservation.dto.HoldSlotRequest;
import com.parkmate.reservation.dto.HoldSlotResponse;

public interface ReservationHoldService {

    /**
     * Hold a parking slot temporarily (15 minutes TTL)
     */
    HoldSlotResponse holdSlot(HoldSlotRequest request);

    /**
     * Release a held slot manually (e.g., user cancels or goes back)
     */
    void releaseHold(String holdId);

    /**
     * Count temporary holds for a specific parking lot and time range
     * Used by parking-lot-service to calculate available spots
     */
    Long countTemporaryHolds(Long parkingLotId, String vehicleType, String reservedFrom, Integer assumedStayMinute);

    /**
     * Check if a hold exists and is still valid
     */
    boolean isHoldValid(String holdId);

    /**
     * Get hold details by holdId
     */
    TemporaryReservationHold getHold(String holdId);
}
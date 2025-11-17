package com.parkmate.client;

import com.parkmate.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "parking-lot-service")
public interface ParkingLotClient {

    /**
     * Activates a parking lot after operational fee payment is confirmed
     * This endpoint is called from payment-service webhook handler
     *
     * @param lotId The parking lot ID to activate
     * @return Response indicating success or failure
     */
    @PutMapping("/internal/parking-lots/{lotId}/activate")
    ApiResponse<Void> activateParkingLot(@PathVariable("lotId") Long lotId);
}
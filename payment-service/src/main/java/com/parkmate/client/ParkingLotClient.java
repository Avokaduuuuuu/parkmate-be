package com.parkmate.client;

import com.parkmate.client.dto.ParkingLotBasicInfo;
import com.parkmate.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * Get total revenue from MEMBER+RESERVATION sessions for a parking lot in a date range
     *
     * @param lotId  The parking lot ID
     * @param from   Start date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @param to     End date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @return Total revenue amount
     */
    @GetMapping("/internal/sessions/revenue/member-reservation")
    ApiResponse<BigDecimal> getMemberReservationRevenue(
            @RequestParam("lotId") Long lotId,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    );

    /**
     * Get total revenue from MEMBER+WALK_IN sessions for a parking lot in a date range
     *
     * @param lotId  The parking lot ID
     * @param from   Start date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @param to     End date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @return Total revenue amount
     */
    @GetMapping("/internal/sessions/revenue/member-walkin")
    ApiResponse<BigDecimal> getMemberWalkInRevenue(
            @RequestParam("lotId") Long lotId,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    );

    /**
     * Get all parking lots for a partner
     *
     * @param partnerId The partner ID
     * @return List of parking lot basic info (id, name, partnerId)
     */
    @GetMapping("/internal/parking-lots/partner/{partnerId}")
    ApiResponse<List<ParkingLotBasicInfo>> getParkingLotsByPartner(@PathVariable("partnerId") Long partnerId);

    /**
     * Get all parking lots in the system (across all partners)
     *
     * @return List of all parking lot basic info (id, name, partnerId)
     */
    @GetMapping("/internal/parking-lots/all")
    ApiResponse<List<ParkingLotBasicInfo>> getAllParkingLots();
}
package com.parkmate.parking_lot;

import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API controller for parking lot operations
 * These endpoints are called by other microservices and should not be exposed externally
 */
@RestController
@RequestMapping("/internal/parking-lots")
@RequiredArgsConstructor
@Slf4j
@Hidden // Hide from Swagger documentation as these are internal endpoints
public class ParkingLotInternalController {

    private final ParkingLotService parkingLotService;

    /**
     * Activates a parking lot after operational fee payment is confirmed
     * This endpoint is called from payment-service webhook handler
     *
     * @param lotId The parking lot ID to activate
     * @return Success response
     */
    @PutMapping("/{lotId}/activate")
    public ResponseEntity<ApiResponse<?>> activateParkingLot(@PathVariable Long lotId) {
        log.info("Received activation request for parking lot: {}", lotId);

        parkingLotService.activateParkingLot(lotId);

        return ResponseEntity.ok(ApiResponse.success(
                "Parking lot activated successfully",
                null
        ));
    }
}

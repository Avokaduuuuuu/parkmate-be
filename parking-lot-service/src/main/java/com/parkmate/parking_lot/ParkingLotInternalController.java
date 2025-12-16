package com.parkmate.parking_lot;

import com.parkmate.common.ApiResponse;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.dto.resp.ParkingLotBasicInfo;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final ParkingLotRepository parkingLotRepository;

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

    /**
     * Get all parking lots in the system (across all partners)
     * Used by payment-service to create withdrawal periods for all lots
     *
     * @return List of all parking lot basic info
     */
    @GetMapping("/all")
    public ApiResponse<List<ParkingLotBasicInfo>> getAllParkingLots() {
        log.info("Getting all parking lots");

        List<ParkingLotBasicInfo> parkingLots = parkingLotService.getAllParkingLotsBasicInfo();

        log.info("Found {} parking lots", parkingLots.size());
        return ApiResponse.success(parkingLots);
    }

    /**
     * Get all parking lots for a specific partner
     * Used by payment-service to calculate partner-specific revenues
     *
     * @param partnerId The partner ID
     * @return List of parking lot basic info for the partner
     */
    @GetMapping("/partner/{partnerId}")
    public ApiResponse<List<ParkingLotBasicInfo>> getParkingLotsByPartner(@PathVariable Long partnerId) {
        log.info("Getting parking lots for partner: {}", partnerId);

        List<ParkingLotBasicInfo> parkingLots = parkingLotService.getParkingLotsByPartner(partnerId);

        log.info("Found {} parking lots for partner {}", parkingLots.size(), partnerId);
        return ApiResponse.success(parkingLots);
    }

    /**
     * Get parking lot name by ID
     * Used by payment-service for notification messages
     *
     * @param lotId The parking lot ID
     * @return Parking lot name info
     */
    @GetMapping("/{lotId}/name")
    public ApiResponse<ParkingLotNameDto> getParkingLotName(@PathVariable Long lotId) {
        log.info("Getting name for parking lot: {}", lotId);

        ParkingLotEntity parkingLot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        return ApiResponse.success(new ParkingLotNameDto(parkingLot.getId(), parkingLot.getName()));
    }

    @GetMapping("/count")
    public ApiResponse<Map<Long, Long>> getParkingLotCount(@RequestParam List<Long> partnerIds) {
        return ApiResponse.success(parkingLotService.getParkingLotCountByPartner(partnerIds));
    }

    public record ParkingLotNameDto(Long id, String name) {
    }
}

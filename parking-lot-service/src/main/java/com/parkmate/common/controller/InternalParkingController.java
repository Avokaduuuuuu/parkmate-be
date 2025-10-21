package com.parkmate.common.controller;

import com.parkmate.common.ApiResponse;
import com.parkmate.parking_lot.ParkingLotService;
import com.parkmate.spot.SpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API endpoints for inter-service communication.
 * These endpoints are only accessible from other microservices (service-to-service calls).
 * They return minimal data for performance optimization.
 */
@Slf4j
@RestController
@RequestMapping("/internal/parking-service")
@RequiredArgsConstructor
@Tag(
    name = "Internal Parking Service API",
    description = "Internal endpoints for service-to-service communication. Used by user-service for reservation operations."
)
public class InternalParkingController {

    private final ParkingLotService parkingLotService;
    private final SpotService spotService;

    @GetMapping("/lots/{id}/name")
    @Operation(
        summary = "Get parking lot name by ID",
        description = "Retrieve only the ID and name of a parking lot. Used internally by reservation service."
    )
    public ResponseEntity<?> getParkingLotName(
        @PathVariable("id")
        @Parameter(description = "Parking lot ID", required = true, example = "1")
        Long id
    ) {
        log.debug("Getting parking lot name for ID: {}", id);
        var parkingLot = parkingLotService.getParkingLotById(id);
        var response = new ParkingLotNameDto(parkingLot.getId(), parkingLot.getName());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Parking lot name fetched successfully", response));
    }

    @GetMapping("/spots/{id}/name")
    @Operation(
        summary = "Get spot name by ID",
        description = "Retrieve only the ID and name of a parking spot. Used internally by reservation service."
    )
    public ResponseEntity<?> getSpotName(
        @PathVariable("id")
        @Parameter(description = "Spot ID", required = true, example = "1")
        Long id
    ) {
        log.debug("Getting spot name for ID: {}", id);
        var spot = spotService.findById(id);
        var response = new SpotNameDto(spot.getId(), spot.getName());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Spot name fetched successfully", response));
    }

    /**
     * DTO for parking lot name response
     */
    public record ParkingLotNameDto(Long id, String name) {}

    /**
     * DTO for spot name response
     */
    public record SpotNameDto(Long id, String name) {}
}
package com.parkmate.common.controller;

import com.parkmate.common.ApiResponse;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.ParkingLotService;
import com.parkmate.pricing_rule.PricingRuleService;
import com.parkmate.spot.SpotService;
import com.parkmate.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
    private final PricingRuleService pricingRuleService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/lots/{id}/name")
    @Operation(
            summary = "Get parking lot name by ID",
            description = "Retrieve only the ID and name of a parking lot. Used internally by reservation service."
    )
    public ResponseEntity<?> getParkingLotName(
            @PathVariable @Parameter(description = "Parking lot ID", required = true, example = "1")
            Long id
    ) {
        log.debug("Getting parking lot name for ID: {}", id);
        var parkingLot = parkingLotService.getParkingLotById(id);
        var response = new ParkingLotInternalDto(parkingLot.getId(), parkingLot.getName(), parkingLot.getHorizonTime());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Parking lot name fetched successfully", response));
    }

    @GetMapping("/lots/{id}/partner")
    @Operation(
            summary = "Get parking lot partner ID by parking lot ID",
            description = "Retrieve the partner (owner) ID for a parking lot. Used internally for wallet transactions."
    )
    public ResponseEntity<?> getPartnerIdByParkingLotId(
            @PathVariable @Parameter(description = "Parking lot ID", required = true, example = "1")
            Long id
    ) {
        log.debug("Getting partner ID for parking lot ID: {}", id);
        var parkingLot = parkingLotService.getParkingLotById(id);
        var response = new PartnerIdDto(parkingLot.getPartnerId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Partner ID fetched successfully", response));
    }

    @GetMapping("/spots/{id}/name")
    @Operation(
            summary = "Get spot name by ID",
            description = "Retrieve only the ID and name of a parking spot. Used internally by reservation service."
    )
    public ResponseEntity<?> getSpotName(
            @PathVariable @Parameter(description = "Spot ID", required = true, example = "1")
            Long id
    ) {
        log.debug("Getting spot name for ID: {}", id);
        var spot = spotService.findById(id);
        var response = new SpotNameDto(spot.getId(), spot.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Spot name fetched successfully", response));
    }

    @GetMapping("/lots/{id}/pricing-rules")
    public ResponseEntity<?> getParkingLotName(
            @PathVariable @Parameter(description = "Parking lot ID", required = true, example = "1") Long id,
            @RequestParam VehicleType vehicleType
    ) {
        var pricingRule = pricingRuleService.findByParkingLotIdAndVehicleType(id, vehicleType);
        var response = new PricingRuleDto(pricingRule.getId(), pricingRule.getVehicleType());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Pricing rule fetched successfully", response));
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<?> getSubscriptionById(
            @PathVariable @Parameter(description = "Subscription ID", required = true, example = "1") Long id
    ) {
        var subscription = subscriptionService.fetchSubscriptionById(id);
        var response = new SubscriptionDto(
                subscription.getId(),
                subscription.getDurationValue(),
                subscription.getName(),
                subscription.getPrice());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Subscription fetched successfully", response));

    }

    /**
     * DTO for parking lot name response
     */
    public record ParkingLotInternalDto(Long id,
                                        String name,
                                        Integer horizonTime) {
    }

    /**
     * DTO for spot name response
     */
    public record SpotNameDto(Long id, String name) {
    }

    public record PricingRuleDto(Long id, VehicleType vehicleType) {
    }

    public record SubscriptionDto(Long id, Integer durationValue, String name, BigDecimal amount) {
    }

    public record PartnerIdDto(Long partnerId) {
    }
}
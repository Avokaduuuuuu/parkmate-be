package com.parkmate.client;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.vehicle.VehicleType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for inter-service communication with parking-lot-service.
 * Uses internal endpoints for performance-optimized data retrieval.
 */
@FeignClient(name = "parking-lot-service")
public interface ParkingLotClient {

    @GetMapping("/internal/parking-service/lots/{id}/name")
    ApiResponse<ParkingLotSimpleDto> getParkingLotName(@PathVariable Long id);

    @GetMapping("/internal/parking-service/spots/{id}/name")
    ApiResponse<SpotNameDto> getSpotName(@PathVariable Long id);

    @GetMapping("/internal/parking-service/lots/{id}/pricing-rules")
    ApiResponse<PricingRuleDto> getPricingRule(@PathVariable Long id, @RequestParam VehicleType vehicleType);

    @GetMapping("/internal/parking-service/subscriptions/{id}")
    ApiResponse<SubscriptionDto> getSubscription(@PathVariable Long id);

    @GetMapping("/api/v1/parking-service/floors/internal/subscription-availability")
    ApiResponse<List<FloorSubscriptionAvailabilityDto>> getFloorSubscriptionAvailability(
            @RequestParam("parkingLotId") Long parkingLotId,
            @RequestParam("vehicleType") VehicleType vehicleType,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );

    @GetMapping("/api/v1/parking-service/areas/internal/subscription-availability")
    ApiResponse<List<AreaSubscriptionAvailabilityDto>> getAreaSubscriptionAvailability(
            @RequestParam("floorId") Long floorId,
            @RequestParam("vehicleType") VehicleType vehicleType,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );

    @GetMapping("/api/v1/parking-service/spots/internal/subscription-availability")
    ApiResponse<List<SpotSubscriptionAvailabilityDto>> getSpotSubscriptionAvailability(
            @RequestParam("areaId") Long areaId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );

    @PostMapping("/internal/parking-service/spots/hold")
    ApiResponse<Boolean> holdSpot(
            @RequestParam("userId") Long userId,
            @RequestParam("spotId") Long spotId
    );


    record ParkingLotSimpleDto(Long id, String name, Integer horizonTime) {
    }

    record SpotNameDto(Long id, String name) {
    }

    record PricingRuleDto(Long id) {
    }

    record SubscriptionDto(Long id, Integer durationValue, String name) {
    }

    record FloorSubscriptionAvailabilityDto(
            Long id,
            Integer floorNumber,
            String floorName,
            Boolean isActive,
            Integer totalSubscriptionSpots,
            Integer availableSubscriptionSpots
    ) {
    }

    record AreaSubscriptionAvailabilityDto(
            Long id,
            String name,
            VehicleType vehicleType,
            Integer totalSpots,
            Double areaTopLeftX,
            Double areaTopLeftY,
            Double areaWidth,
            Double areaHeight,
            Boolean isActive,
            Boolean supportElectricVehicle,
            String areaType,
            Integer availableSubscriptionSpots
    ) {
    }

    record SpotSubscriptionAvailabilityDto(
            Long id,
            String name,
            Double spotTopLeftX,
            Double spotTopLeftY,
            Double spotWidth,
            Double spotHeight,
            String status,
            Boolean isAvailableForSubscription,
            String subscriptionUnavailabilityReason
    ) {
    }
}

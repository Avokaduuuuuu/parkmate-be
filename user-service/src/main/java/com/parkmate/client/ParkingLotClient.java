package com.parkmate.client;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.vehicle.VehicleType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for inter-service communication with parking-lot-service.
 * Uses internal endpoints for performance-optimized data retrieval.
 */
@FeignClient(name = "parking-lot-service")
public interface ParkingLotClient {

    @GetMapping("/internal/parking-service/lots/{id}/name")
    ApiResponse<ParkingLotNameDto> getParkingLotName(@PathVariable Long id);

    @GetMapping("/internal/parking-service/spots/{id}/name")
    ApiResponse<SpotNameDto> getSpotName(@PathVariable Long id);

    @GetMapping("/internal/parking-service/lots/{id}/pricing-rules")
    ApiResponse<PricingRuleDto> getPricingRule(@PathVariable Long id, @RequestParam VehicleType vehicleType);


    record ParkingLotNameDto(Long id, String name, Integer horizonTime) {
    }

    record SpotNameDto(Long id, String name) {
    }

    record PricingRuleDto(Long id) {
    }
}

package com.parkmate.pricing_rule;

import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.req.PricingRuleUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parking-service/pricing-rules")
@RequiredArgsConstructor
@Tag(name = "Pricing Rule API", description = "API for making, retrieving, update pricing rule of a parking lot")
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                            "Fetch Pricing Rules successfully",
                                pricingRuleService.findAllPricingRules(page, size, sortBy, sortOrder)
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable("id") String ruleId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch Pricing Rule by Id successfully",
                                pricingRuleService.findPricingRuleById(UUID.fromString(ruleId))
                        )
                );
    }

    @PostMapping("/{parkingLotId}")
    public ResponseEntity<?> addPricingRule(
            @PathVariable("parkingLotId") Long parkingLotId,
            @RequestBody @Valid PricingRuleCreateRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Pricing Rule is created successfully",
                                pricingRuleService.createPricingRule(parkingLotId, request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePricingRule(
            @PathVariable("id") String id,
            @RequestBody @Valid PricingRuleUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Pricing Rule is updated successfully",
                                pricingRuleService.updatePricingRule(UUID.fromString(id), request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePricingRule(
            @PathVariable("id") String id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Pricing Rule is disable",
                                pricingRuleService.deletePricingRule(UUID.fromString(id))
                        )
                );
    }

}

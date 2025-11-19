package com.parkmate.pricing_rule;

import com.parkmate.common.ApiResponse;
import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.req.PricingRuleUpdateRequest;
import com.parkmate.pricing_rule.dto.req.SyncedPricingRulesUpdateRequest;
import com.parkmate.session.enums.SyncStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable("id") Long ruleId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch Pricing Rule by Id successfully",
                                pricingRuleService.findPricingRuleById(ruleId)
                        )
                );
    }

    @PostMapping("/{parkingLotId}")
    public ResponseEntity<?> addPricingRule(
            @PathVariable("parkingLotId") Long parkingLotId,
            @RequestBody @Valid PricingRuleCreateRequest request
    ) {
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
            @PathVariable("id") Long id,
            @RequestBody @Valid PricingRuleUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Pricing Rule is updated successfully",
                                pricingRuleService.updatePricingRule(id, request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePricingRule(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Pricing Rule is disable",
                                pricingRuleService.deletePricingRule(id)
                        )
                );
    }

    @GetMapping("/{lotId}/sync")
    public ResponseEntity<?> syncPricingRules(
            @PathVariable("lotId") Long lotId,
            @RequestParam("status") SyncStatus status
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch pricing rules of a parking lot",
                                pricingRuleService.findAllSyncPricingRules(lotId, status)
                        )
                );
    }

    @PutMapping("/sync")
    public ResponseEntity<?> syncPricingRules(
            @RequestBody SyncedPricingRulesUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Update Synced Pricing Rules",
                                pricingRuleService.updateSyncedPricingRules(request)
                        )
                );
    }
}

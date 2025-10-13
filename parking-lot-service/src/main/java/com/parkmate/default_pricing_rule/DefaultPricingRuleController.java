package com.parkmate.default_pricing_rule;

import com.parkmate.common.ApiResponse;
import com.parkmate.default_pricing_rule.dto.req.DefaultPricingRuleCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parking-service/default-rule")
@RequiredArgsConstructor
public class DefaultPricingRuleController {
    private final DefaultPricingRuleService defaultPricingRuleService;

    @PostMapping
    @Operation(
            summary = "Create default pricing rule for a parking lot",
            description = """
                Assign a default pricing rule for a specific vehicle type at a parking lot.
                This default rule will be automatically applied when pricing needs to be determined.
                
                **Request Body Fields:**
                - `lotId` (required): ID of the parking lot
                - `pricingRuleId` (required): ID of the pricing rule to set as default
                - `vehicleType` (required): Vehicle type this rule applies to
                
                **Business Rules:**
                - Each parking lot can have only ONE default pricing rule per vehicle type
                - If a default already exists for this vehicle type, it will be replaced
                - The pricing rule must belong to the specified parking lot
                - The pricing rule must support the specified vehicle type
                - Parking lot must be in ACTIVE or PARTNER_CONFIGURATION status
                
                **Use Cases:**
                - Setting up standard pricing during parking lot configuration
                - Updating default pricing when rates change
                - Configuring different defaults for different vehicle types
                - Streamlining the check-in process for customers
                
                **Example Scenarios:**
                1. Set default hourly rate for cars: lotId=1, pricingRuleId=10, vehicleType=CAR_UP_TO_9_SEATS
                2. Set default daily rate for motorbikes: lotId=1, pricingRuleId=15, vehicleType=MOTORBIKE
                3. Set default monthly pass for trucks: lotId=1, pricingRuleId=20, vehicleType=OTHER
                
                **Returns:** Created default pricing rule assignment with confirmation
                """
    )
    public ResponseEntity<?> addNewDefaultPricingRule(
            @Parameter(description = "Default pricing rule creation request", required = true)
            @RequestBody @Valid DefaultPricingRuleCreateRequest request
            ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Successfully added new DefaultPricingRule",
                                defaultPricingRuleService.addNewDefaultPricingRule(request)
                        )
                );
    }

}

package com.parkmate.policy;

import com.parkmate.common.ApiResponse;
import com.parkmate.policy.dto.resp.PolicyResponse;
import com.parkmate.policy.enums.PolicyType;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API controller for policy operations
 * These endpoints are called by other microservices and should not be exposed externally
 */
@RestController
@RequestMapping("/internal/parking-service/policies")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class PolicyInternalController {

    private final PolicyService policyService;

    /**
     * Get policy by parking lot ID and policy type
     * Used by user-service to check cancellation refund policy
     *
     * @param lotId The parking lot ID
     * @param policyType The policy type
     * @return Policy response
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicyByLotIdAndType(
            @RequestParam Long lotId,
            @RequestParam PolicyType policyType) {
        log.info("Getting policy for lot {} and type {}", lotId, policyType);
        PolicyResponse policy = policyService.getPolicyByLotIdAndType(lotId, policyType);
        return ResponseEntity.ok(ApiResponse.success("Policy retrieved successfully", policy));
    }
}

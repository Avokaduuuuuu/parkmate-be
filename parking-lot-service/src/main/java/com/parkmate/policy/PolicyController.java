package com.parkmate.policy;

import com.parkmate.policy.dto.req.PolicyUpdateRequest;
import com.parkmate.policy.dto.req.SyncedPolicyUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-service/policies")
@RequiredArgsConstructor
@Tag(
        name = "Policy Management",
        description = "APIs for managing parking lot policies including check-in/check-out buffers, cancellation rules, and refund policies"
)
public class PolicyController {
    private final PolicyService policyService;

    @GetMapping("/{lotId}/sync")
    @Operation(
            summary = "Pull all policies for a parking lot",
            description = """
                    Retrieves all policies configured for a specific parking lot.
                    Returns all 4 policy types: EARLY_CHECK_IN_BUFFER,
                    LATE_CHECK_IN_CANCEL_AFTER, and EARLY_CANCEL_REFUND_BEFORE
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved policies",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyEntity.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Pulled Policies successfully",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "policyType": "EARLY_CHECK_IN_BUFFER",
                                                  "value": 10
                                                },
                                              
                                                {
                                                  "id": 3,
                                                  "policyType": "LATE_CHECK_IN_CANCEL_AFTER",
                                                  "value": 30
                                                },
                                                {
                                                  "id": 4,
                                                  "policyType": "EARLY_CANCEL_REFUND_BEFORE",
                                                  "value": 60
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Parking lot not found"
            )
    })
    public ResponseEntity<?> pullPolicies(
            @Parameter(
                    description = "ID of the parking lot to retrieve policies for",
                    required = true,
                    example = "1"
            )
            @PathVariable("lotId") Long lotId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Pulled Policies successfully",
                                policyService.pullPolicies(lotId)
                        )
                );
    }

    @PutMapping("/sync")
    @Operation(
            summary = "Sync multiple policies",
            description = """
                    Synchronizes and updates multiple policies at once using their IDs.
                    Typically used to update all 4 policies of a parking lot in a single request.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully synced policies",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Synced Policies successfully",
                                              "data": [
                                                {
                                                  "id": 1,
                                                  "policyType": "EARLY_CHECK_IN_BUFFER",
                                                  "value": 15
                                                },
                                                {
                                                  "id": 2,
                                                  "policyType": "LATE_CHECK_OUT_BUFFER",
                                                  "value": 20
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - missing required policy types or duplicate types"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "One or more policies not found"
            )
    })
    public ResponseEntity<?> updatePolicies(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of policy IDs to sync and update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SyncedPolicyUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "policyIds": [1, 2, 3, 4]
                                            }
                                            """
                            )
                    )
            )
            @RequestBody SyncedPolicyUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Synced Policies successfully",
                                policyService.syncPolicies(request.policyIds())
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a single policy",
            description = """
                    Updates a specific policy by its ID. 
                    Can update the policy's value (time in minutes) and other configurable fields.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated policy",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Updated policy successfully",
                                              "data": {
                                                "id": 1,
                                                "policyType": "EARLY_CHECK_IN_BUFFER",
                                                "value": 15
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Policy not found"
            )
    })
    public ResponseEntity<?> updatePolicy(
            @Parameter(
                    description = "ID of the policy to update",
                    required = true,
                    example = "1"
            )
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated policy data",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "value": 15
                                            }
                                            """
                            )
                    )
            )
            @RequestBody PolicyUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Updated policy successfully",
                                policyService.updatePolicy(id, request)
                        )
                );
    }
}
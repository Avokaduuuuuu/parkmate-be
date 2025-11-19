package com.parkmate.subscription;

import com.parkmate.subscription.dto.req.SubscriptionCreateRequest;
import com.parkmate.subscription.dto.req.SubscriptionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-service/subscriptions")
@RequiredArgsConstructor
@Tag(
        name = "Subscription Management",
        description = "APIs for managing parking lot subscription packages. " +
                "Partners can create monthly, quarterly, or yearly subscription plans " +
                "for different vehicle types with customized pricing."
)
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "Retrieve all subscription packages",
            description = "Fetch a paginated list of subscription packages with optional filtering and sorting. " +
                    "Supports filtering by lot ID, vehicle type, duration type, and active status. " +
                    "Results can be sorted by any field in ascending or descending order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved subscription packages",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<?> fetchAll(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userHeaderId,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort order: ASC or DESC", example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,

            @Parameter(description = "Filter parameters for subscriptions")
            SubscriptionFilterParams filterParams
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Fetching all subscriptions.",
                                subscriptionService.fetchAllSubscriptions(userHeaderId, page, size, sortBy, sortOrder, filterParams)
                        )
                );
    }

    @Operation(
            summary = "Get subscription package by ID",
            description = "Retrieve detailed information about a specific subscription package by its unique identifier. " +
                    "Returns package details including name, description, vehicle type, duration, price, and associated parking lot."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved subscription package",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subscription package not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> fetchById(
            @Parameter(description = "Unique identifier of the subscription package", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Fetch Subscription by Id " + id,
                                subscriptionService.fetchSubscriptionById(id)
                        )
                );
    }

    @Operation(
            summary = "Create new subscription package",
            description = "Create a new subscription package for a parking lot. " +
                    "Partners can define the package name, description, vehicle type (BIKE, MOTORBIKE, CAR_UP_TO_9_SEATS), " +
                    "duration type (MONTHLY, QUARTERLY, YEARLY), and price. " +
                    "The subscription will be associated with the specified parking lot."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subscription package created successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parking lot not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<?> addSubscription(
            @Parameter(
                    description = "Subscription package details including name, description, vehicle type, duration, price, and lot ID",
                    required = true
            )
            @RequestBody @Valid SubscriptionCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Created Subscription",
                                subscriptionService.addSubscription(request)
                        )
                );
    }

    @Operation(
            summary = "Update subscription package",
            description = "Update an existing subscription package. " +
                    "Partners can modify the package name, description, price, and active status. " +
                    "Vehicle type and duration type cannot be changed after creation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subscription package updated successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subscription package not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscription(
            @Parameter(description = "Unique identifier of the subscription package to update", required = true, example = "1")
            @PathVariable("id") Long id,

            @Parameter(
                    description = "Updated subscription details (only provided fields will be updated)",
                    required = true
            )
            @RequestBody @Valid SubscriptionUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Updated Subscription",
                                subscriptionService.updateSubscription(request, id)
                        )
                );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete subscription package",
            description = "Delete an existing subscription package. "
    )
    public ResponseEntity<?> deleteSubscription(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        com.parkmate.common.ApiResponse.success(
                                "Updated Subscription",
                                subscriptionService.deleteSubscription(id)
                        )
                );
    }
}
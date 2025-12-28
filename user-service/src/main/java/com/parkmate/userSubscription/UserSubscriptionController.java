package com.parkmate.userSubscription;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.userSubscription.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/user-subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscription Management", description = "APIs for managing user parking subscriptions")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping
    @Operation(
            summary = "Subscribe to parking lot package",
            description = """
                    Purchase a subscription package for regular parking at a specific lot.
                    
                    **Request Body (CreateUserSubscriptionRequest):**
                    - `subscriptionId` (required): ID of the subscription package to purchase
                    - `vehicleId` (required): ID of the vehicle for this subscription
                    - `userId` (optional): User ID (auto-filled from auth if not provided)
                    - `paymentMethod` (optional): Payment method used
                    - `transactionId` (optional): Payment transaction reference
                    
                    **Returns (UserSubscriptionResponse):**
                    - User subscription ID
                    - Subscription package details (name, duration, price)
                    - Vehicle information
                    - Start and end dates
                    - Payment status
                    - Status: ACTIVE
                    
                    **Business Process:**
                    1. Validates subscription package exists and is active
                    2. Checks vehicle ownership
                    3. Verifies no conflicting active subscriptions
                    4. Calculates start and end dates based on duration
                    5. Processes payment (if integrated)
                    6. Creates user subscription record
                    7. Sends confirmation notification
                    
                    **Business Rules:**
                    - User can have one active subscription per vehicle per lot
                    - Subscription duration based on package (MONTHLY, QUARTERLY, YEARLY)
                    - Auto-renewal can be configured
                    - Start date is immediate or scheduled
                    
                    **Use Cases:**
                    - User buying monthly parking pass
                    - Corporate subscription for employee parking
                    - Long-term parking commitment with discount
                    
                    **Error Cases:**
                    - SUBSCRIPTION_NOT_FOUND: Package ID invalid
                    - SUBSCRIPTION_INACTIVE: Package no longer available
                    - VEHICLE_NOT_FOUND: Invalid vehicle ID
                    - ACTIVE_SUBSCRIPTION_EXISTS: User already has active subscription
                    - PAYMENT_FAILED: Payment processing failed
                    """
    )
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> createUserSubscription(
            @RequestBody CreateUserSubscriptionRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userSubscriptionService.create(request, userIdHeader)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user subscription by ID",
            description = """
                    Retrieve detailed information about a specific user subscription.
                    
                    **Path Parameters:**
                    - `id` (required): User subscription ID
                    
                    **Returns (UserSubscriptionResponse):**
                    - Subscription ID and status
                    - Subscription package details
                    - Vehicle information
                    - User information
                    - Start and end dates
                    - Auto-renewal status
                    - Payment information
                    - Created and updated timestamps
                    
                    **Access Control:**
                    - USER: Can view their own subscriptions
                    - PARTNER: Can view subscriptions for their lots
                    - ADMIN: Can view any subscription
                    
                    **Error Cases:**
                    - SUBSCRIPTION_NOT_FOUND: Subscription ID doesn't exist
                    - UNAUTHORIZED: User trying to view another user's subscription
                    """
    )
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getUserSubscriptionById(
            @Parameter(description = "Unique identifier of the user subscription", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.findById(id)));
    }

    @GetMapping
    @Operation(
            summary = "Get user subscriptions with filtering and pagination",
            description = """
                    Retrieve user's subscriptions with optional filtering and pagination.
                    
                    **Query Parameters:**
                    - `page` (optional): Page number, zero-based (default: 0)
                    - `size` (optional): Page size (default: 10)
                    - `sortBy` (optional): Sort field (default: createdAt)
                    - `sortOrder` (optional): Sort direction (default: asc)
                    
                    **Filter Criteria (UserSubscriptionSearchCriteria):**
                    - `userId` (optional): Filter by user (admin/partner only)
                    - `subscriptionId` (optional): Filter by subscription package
                    - `vehicleId` (optional): Filter by vehicle
                    - `status` (optional): Filter by status (ACTIVE, EXPIRED, CANCELLED)
                    
                    **Authentication Behavior:**
                    - Regular users see only their own subscriptions
                    - Partners see subscriptions for their parking lots
                    - Admin sees all subscriptions
                    
                    **Returns:** Paginated list of subscriptions with:
                    - Subscription details and status
                    - Package information
                    - Vehicle details
                    - Validity period
                    - Payment information
                    
                    **Access Control:**
                    - USER: Views only own subscriptions
                    - PARTNER: Views subscriptions for their lots
                    - ADMIN: Views all with filtering
                    """
    )
    public ResponseEntity<ApiResponse<Page<UserSubscriptionResponse>>> getUserSubscriptions(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,

            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,

            @ModelAttribute UserSubscriptionSearchCriteria searchCriteria
    ) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.findAll(page, size, sortBy, sortOrder, userIdHeader, searchCriteria)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update user subscription",
            description = """
                    Update user subscription details or status.
                    
                    **Path Parameters:**
                    - `id` (required): User subscription ID to update
                    
                    **Request Body (UpdateUserSubscriptionRequest) - All optional:**
                    - `status`: Update status (ACTIVE, CANCELLED, EXPIRED)
                    - `endDate`: Extend or modify end date
                    - `autoRenew`: Enable/disable auto-renewal
                    - `notes`: Administrative notes
                    
                    **Returns (UserSubscriptionResponse):** Updated subscription
                    
                    **Access Control:**
                    - USER: Can cancel own subscriptions (status=CANCELLED)
                    - PARTNER: Can modify subscriptions for their lots
                    - ADMIN: Can modify any subscription
                    
                    **Business Rules:**
                    - Cannot reactivate expired subscriptions
                    - Cancellation may have refund implications based on policy
                    - Status changes are logged for audit
                    - End date can only be extended, not shortened (with active status)
                    
                    **Use Cases:**
                    - User cancelling monthly subscription
                    - Admin extending subscription as compensation
                    - Partner modifying subscription terms
                    - Enabling/disabling auto-renewal
                    
                    **Error Cases:**
                    - SUBSCRIPTION_NOT_FOUND: ID doesn't exist
                    - UNAUTHORIZED: User trying to modify others' subscription
                    - INVALID_STATUS_TRANSITION: Cannot change from current to new status
                    - CANNOT_MODIFY_EXPIRED: Cannot modify expired subscriptions
                    
                    **Important Notes:**
                    - Cancellation is effective immediately
                    - Refunds are processed separately if applicable
                    - User loses subscription benefits after cancellation
                    """
    )
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> updateUserSubscription(
            @Parameter(description = "User subscription ID to update", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody UpdateUserSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user subscription",
            description = """
                    Permanently delete a user subscription.
                    
                    **Path Parameters:**
                    - `id` (required): User subscription ID to delete
                    
                    **Behavior:**
                    - Hard delete - removes subscription from database
                    - Associated payment records are preserved
                    - Cannot be undone
                    - Subscription benefits are immediately revoked
                    
                    **Returns:** Success confirmation message (HTTP 204 No Content)
                    
                    **Access Control:** Requires ADMIN role
                    
                    **Use Cases:**
                    - Admin removing test/duplicate subscriptions
                    - Data cleanup operations
                    - Correcting subscription errors
                    
                    **Business Impact:**
                    - User loses subscription benefits immediately
                    - No refund is automatically processed
                    - Historical records are removed
                    
                    **Error Cases:**
                    - SUBSCRIPTION_NOT_FOUND: Subscription ID doesn't exist
                    - ACTIVE_SUBSCRIPTION_DELETE: Cannot delete active subscriptions (cancel first)
                    
                    **Note:** For user-initiated cancellations, use PUT with status=CANCELLED instead.
                    This provides better audit trail and refund processing.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deleteUserSubscription(
            @Parameter(description = "User subscription ID to delete", required = true, example = "1")
            @PathVariable Long id
    ) {
        userSubscriptionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("User subscription deleted successfully"));
    }

    @GetMapping("/floors/availability")
    @Operation(
            summary = "Get floor subscription availability",
            description = """
                    Get list of floors with available subscription spots for a parking lot.
                    
                    **Query Parameters:**
                    - `parkingLotId` (required): ID of the parking lot
                    - `vehicleId` (required): ID of user's vehicle (determines vehicle type)
                    - `subscriptionPackageId` (required): ID of subscription package (determines duration)
                    - `startDate` (required): Subscription start date/time
                    
                    **Returns:** List of floors with:
                    - Floor details (id, name, number)
                    - Total subscription spots count
                    - Available subscription spots count (format: "30/50")
                    
                    **Business Logic:**
                    - Only shows floors matching vehicle type
                    - Calculates availability based on startDate + package duration
                    - Excludes spots with ACTIVE or PENDING_PAYMENT subscriptions in that period
                    """
    )
    public ResponseEntity<ApiResponse<List<?>>> getFloorAvailability(
            @Parameter(description = "Parking lot ID", required = true, example = "1")
            @RequestParam Long parkingLotId,

            @Parameter(description = "User's vehicle ID", required = true, example = "1")
            @RequestParam Long vehicleId,

            @Parameter(description = "Subscription package ID", required = true, example = "1")
            @RequestParam Long subscriptionPackageId,

            @Parameter(description = "Start date/time", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
    ) {
        List<?> floors = userSubscriptionService.getFloorAvailability(parkingLotId, vehicleId, subscriptionPackageId, startDate);
        return ResponseEntity.ok(ApiResponse.success(floors));
    }

    @GetMapping("/areas/availability")
    @Operation(
            summary = "Get area subscription availability",
            description = """
                    Get list of areas with available subscription spots for a floor.
                    
                    **Query Parameters:**
                    - `floorId` (required): ID of the floor
                    - `vehicleId` (required): ID of user's vehicle (determines vehicle type)
                    - `subscriptionPackageId` (required): ID of subscription package (determines duration)
                    - `startDate` (required): Subscription start date/time
                    
                    **Returns:** List of areas with:
                    - Area details (id, name, coordinates for map display)
                    - Vehicle type
                    - Total spots count
                    - Available subscription spots count
                    - Whether it supports electric vehicles
                    
                    **Business Logic:**
                    - Only shows SUBSCRIPTION_ONLY areas
                    - Filters by vehicle type
                    - Calculates availability for the subscription period
                    """
    )
    public ResponseEntity<ApiResponse<List<?>>> getAreaAvailability(
            @Parameter(description = "Floor ID", required = true, example = "1")
            @RequestParam Long floorId,

            @Parameter(description = "User's vehicle ID", required = true, example = "1")
            @RequestParam Long vehicleId,

            @Parameter(description = "Subscription package ID", required = true, example = "1")
            @RequestParam Long subscriptionPackageId,

            @Parameter(description = "Start date/time", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
    ) {
        List<?> areas = userSubscriptionService.getAreaAvailability(floorId, vehicleId, subscriptionPackageId, startDate);
        return ResponseEntity.ok(ApiResponse.success(areas));
    }

    @GetMapping("/spots/availability")
    @Operation(
            summary = "Get spot subscription availability",
            description = """
                    Get list of spots with availability status for an area.
                    
                    **Query Parameters:**
                    - `areaId` (required): ID of the area
                    - `subscriptionPackageId` (required): ID of subscription package (determines duration)
                    - `startDate` (required): Subscription start date/time
                    
                    **Returns:** List of spots with:
                    - Spot details (id, name, coordinates for map display)
                    - Availability status for subscription
                    - Unavailability reason if not available:
                      * NOT_SUBSCRIPTION_AREA: Area is not subscription-only
                      * ALREADY_ASSIGNED: Spot has active subscription in that period
                      * SPOT_HELD: Spot is currently held by another user
                    
                    **Business Logic:**
                    - Shows all spots in the area
                    - Marks spots as unavailable if:
                      1. Area is not SUBSCRIPTION_ONLY type
                      2. Spot already has ACTIVE/PENDING_PAYMENT subscription overlapping the period
                      3. Spot is currently held (15-minute hold from another user)
                    """
    )
    public ResponseEntity<ApiResponse<List<?>>> getSpotAvailability(
            @Parameter(description = "Area ID", required = true, example = "1")
            @RequestParam Long areaId,

            @Parameter(description = "Subscription package ID", required = true, example = "1")
            @RequestParam Long subscriptionPackageId,

            @Parameter(description = "Start date/time", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
    ) {
        List<?> spots = userSubscriptionService.getSpotAvailability(areaId, subscriptionPackageId, startDate);
        return ResponseEntity.ok(ApiResponse.success(spots));
    }

    @PostMapping("/hold-spot")
    @Operation(
            summary = "Hold a spot for subscription",
            description = """
                    Hold a spot for 15 minutes while user completes payment.
                    
                    **Query Parameters:**
                    - `spotId` (required): ID of the spot to hold
                    
                    **Returns:** Boolean indicating success
                    
                    **Business Logic:**
                    - Spot is held for 15 minutes (600 seconds)
                    - During hold period, spot appears unavailable to other users
                    - Hold is automatically released after timeout
                    - User can release hold manually by not completing payment
                    - Hold uses Redis for fast concurrent access control
                    
                    **Use Case:**
                    User selects spot → Hold spot → Complete payment → Create subscription
                    If payment fails/cancelled → Hold automatically expires
                    
                    **Error Cases:**
                    - SPOT_NOT_FOUND: Invalid spot ID
                    - SPOT_ALREADY_HELD: Spot is currently held by another user
                    - SPOT_UNAVAILABLE: Spot has active subscription
                    """
    )
    public ResponseEntity<ApiResponse<Boolean>> holdSpot(
            @Parameter(description = "Spot ID to hold", required = true, example = "1")
            @RequestParam Long spotId,

            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        Long userId = Long.parseLong(userIdHeader);
        Boolean result = userSubscriptionService.holdSpot(userId, spotId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/hold-spot")
    @Operation(
            summary = "Release a held spot",
            description = """
                    Manually release a spot that was previously held for subscription.
                    
                    **Query Parameters:**
                    - `spotId` (required): ID of the spot to release
                    
                    **Returns:** Spot details after release
                    
                    **Business Logic:**
                    - Releases the hold on a spot that was previously held by the user
                    - Only the user who held the spot can release it
                    - Spot becomes immediately available to other users
                    - If hold already expired, operation still succeeds
                    
                    **Use Cases:**
                    - User cancels subscription payment
                    - User decides not to subscribe
                    - User wants to select a different spot
                    
                    **Error Cases:**
                    - SPOT_NOT_FOUND: Invalid spot ID
                    - UNAUTHORIZED: Spot is held by another user
                    - SPOT_NOT_HELD: Spot is not currently held
                    """
    )
    public ResponseEntity<ApiResponse<?>> releaseSpot(
            @Parameter(description = "Spot ID to release", required = true, example = "1")
            @RequestParam Long spotId,

            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        var result = userSubscriptionService.releaseSpot(spotId, userIdHeader);
        return ResponseEntity.ok(ApiResponse.success("Spot hold released successfully", result));
    }

    @PutMapping("/{id}/sync")
    public ResponseEntity<?> syncUserSubscriptionStatus(@PathVariable Long id) {
        userSubscriptionService.syncUserSubscription(id);
        return ResponseEntity.ok(
                ApiResponse.success("sync successfully")
        );
    }

    @GetMapping("/{lotId}/sync")
    public ResponseEntity<?> getUserSubscriptionForSyncing(@PathVariable Long lotId) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.getUserSubscriptionSync(lotId)));
    }

    @PostMapping("/{id}/renewal-decision")
    @Operation(
            summary = "Set subscription renewal decision",
            description = """
                    Allow users to decide whether to continue auto-renewal when subscription is expiring.

                    **Path Parameters:**
                    - `id` (required): User subscription ID

                    **Request Body:**
                    - `continueRenewal` (required): true to keep auto-renewal enabled, false to disable

                    **Returns (UserSubscriptionResponse):** Updated subscription with new autoRenew value

                    **Business Logic:**
                    - Updates the autoRenew flag based on user decision
                    - If true: Subscription will auto-renew on endDate (if balance sufficient)
                    - If false: Subscription will expire on endDate (no auto-renewal)
                    - User can change their decision multiple times before expiration

                    **Use Cases:**
                    - User receives "expiring in 7 days" notification
                    - User clicks notification action button to decide
                    - User changes mind and updates decision later

                    **Access Control:**
                    - USER: Can only update own subscriptions
                    - ADMIN: Can update any subscription

                    **Error Cases:**
                    - SUBSCRIPTION_NOT_FOUND: Invalid subscription ID
                    - UNAUTHORIZED: User trying to modify others' subscription
                    - SUBSCRIPTION_ALREADY_EXPIRED: Cannot modify expired subscriptions
                    """
    )
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> setRenewalDecision(
            @Parameter(description = "User subscription ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody RenewalDecisionRequest request
    ) {
        UserSubscriptionResponse response = userSubscriptionService.setRenewalDecision(id, request.getContinueRenewal());
        return ResponseEntity.ok(ApiResponse.success("Renewal decision updated successfully", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel subscription",
            description = """
                    Cancel a user subscription with conditional refund.

                    **Path Parameters:**
                    - `id` (required): User subscription ID to cancel

                    **Request Body (CancelSubscriptionRequest):**
                    - `reason` (optional): Reason for cancellation

                    **Returns (UserSubscriptionResponse):** Cancelled subscription with refund details

                    **Refund Eligibility Rules:**
                    - **NO REFUND** if subscription has been used (has any parking session)
                    - **NO REFUND** if more than 1/2 of the subscription period has passed
                    - **FULL REFUND** if subscription hasn't been used AND within first half of period
                    - **FULL REFUND** if subscription hasn't started yet

                    **Business Process:**
                    1. Validates subscription exists and belongs to user
                    2. Checks if subscription is already cancelled or expired
                    3. Calls parking-lot-service to check if subscription has been used
                    4. Calculates if current date is within first half of subscription period
                    5. If eligible: processes full refund to user wallet
                    6. Updates subscription status to CANCELLED
                    7. Sends cancellation notification

                    **Access Control:**
                    - USER: Can only cancel own subscriptions
                    - ADMIN: Can cancel any subscription

                    **Error Cases:**
                    - USER_SUBSCRIPTION_NOT_FOUND: Invalid subscription ID
                    - USER_SUBSCRIPTION_NOT_BELONG_TO_USER: Subscription belongs to another user
                    - USER_SUBSCRIPTION_ALREADY_CANCELLED: Subscription already cancelled
                    - USER_SUBSCRIPTION_EXPIRED: Cannot cancel expired subscription
                    - USER_SUBSCRIPTION_CANCEL_FAILED: Refund processing failed

                    **Important Notes:**
                    - Cancellation is immediate and irreversible
                    - If refund is processed, amount is credited to user wallet
                    - User will receive push notification about cancellation
                    - Cancelled subscription cannot be reactivated
                    """
    )
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> cancelSubscription(
            @Parameter(description = "User subscription ID to cancel", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody CancelSubscriptionRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        UserSubscriptionResponse response = userSubscriptionService.cancelSubscription(id, request, userIdHeader);
        String message = response.getRefundAmount() != null && response.getRefundAmount().compareTo(java.math.BigDecimal.ZERO) > 0
                ? "Đã hủy vé tháng và hoàn tiền " + response.getRefundAmount() + " VND"
                : "Đã hủy vé tháng (không hoàn tiền do đã sử dụng hoặc quá 1/2 thời hạn)";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

}

package com.parkmate.session;

import com.parkmate.common.ApiResponse;
import com.parkmate.session.dto.req.SessionCreateRequest;
import com.parkmate.session.dto.req.SessionSyncRequest;
import com.parkmate.session.dto.req.SessionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking-service/sessions")
@RequiredArgsConstructor
@Tag(name = "Parking Session API", description = "API for managing and calculating parking sessions and vehicle entry/exit tracking")
public class SessionController {

    private final SessionService sessionService;


    @Operation(
            summary = "Get parking session by card UUID",
            description = """
                Retrieve detailed information about a specific parking session using the access card UUID.
                
                **Path Parameters:**
                - `cardUUID` (required): The unique identifier of the access card associated with the parking session
                
                **Returns:** Complete session details including:
                - User and vehicle information
                - Entry and exit timestamps
                - Parking spot and location details
                - Applied pricing rule
                - Session duration and calculated fees
                - Current session status (ACTIVE, COMPLETED, CANCELLED)
                - Authentication method used
                """
    )
    @GetMapping("/{cardUUID}")
    public ResponseEntity<?> getSession(
            @Parameter(description = "Unique identifier of the access card", required = true, example = "CARD-UUID-123456")
            @PathVariable("cardUUID") String cardUUID
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch session successfully",
                                sessionService.getSession(cardUUID)
                        )
                );
    }

    @GetMapping
    @Operation(
            summary = "Get all parking sessions with pagination and filtering",
            description = """
                Retrieve a paginated and filtered list of parking sessions with sorting capabilities.
                
                **Query Parameters:**
                - `page` (optional): Page number (default: 0, zero-based index)
                - `size` (optional): Page size (default: 10, maximum: 100)
                - `sortBy` (optional): Sort field (default: id)
                  * Available fields: id, entryTime, exitTime, userId, status, totalAmount, durationMinute
                - `sortOrder` (optional): Sort direction ASC/DESC (default: ASC)
                
                **Filter Parameters** (all optional):
                - `lotId`: Filter by parking lot ID
                - `vehicleType`: Filter by vehicle type (BIKE, MOTORBIKE, CAR_UP_TO_9_SEATS, OTHER)
                - `authMethod`: Filter by authentication method (BLE, NFC_CARD, QR)
                - `sessionType`: Filter by session type (MEMBER, OCCASIONAL)
                - `referenceType`: Filter by reference type (WALK_IN, RESERVATION, SUBSCRIPTION)
                - `startTime`: Filter sessions with entry time >= this timestamp (ISO 8601 format)
                - `endTime`: Filter sessions with exit time <= this timestamp (ISO 8601 format)
                - `totalLessThan`: Filter sessions with total amount < this value (VND)
                - `totalMoreThan`: Filter sessions with total amount > this value (VND)
                - `sessionStatus`: Filter by session status (ACTIVE, COMPLETED)
                - `licensePlate`: Filter by exact license plate match
                - `durationMinuteGreaterThan`: Filter sessions with duration >= this value (minutes)
                - `durationMinuteLessThan`: Filter sessions with duration <= this value (minutes)
                
                **Returns:** Paginated response containing:
                - List of session records matching filter criteria
                - Total number of matching records
                - Current page number and size
                - Total number of pages
                
                **Use Cases:**
                - Monitoring active parking sessions in real-time
                - Reviewing historical parking data for specific time periods
                - Generating revenue reports and usage analytics
                - Auditing parking lot usage patterns
                - Tracking member vs occasional user sessions
                - Analyzing parking duration patterns
                
                **Example Usage:**
                - Get all active sessions: `?sessionStatus=ACTIVE`
                - Get sessions over 2 hours: `?durationMinuteGreaterThan=120`
                - Get sessions in date range: `?startTime=2025-11-01T00:00:00&endTime=2025-11-01T23:59:59`
                - Get high-value sessions: `?totalMoreThan=100000`
                """
    )
    public ResponseEntity<?> getAllSessions(
            @Parameter(description = "Page number (zero-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page (max: 100)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field name to sort by (id, entryTime, exitTime, userId, status, totalAmount, durationMinute)", example = "entryTime")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (ASC for ascending, DESC for descending)", example = "DESC")
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,

            @Parameter(description = "Filter parameters for advanced session filtering")
            SessionFilterParams params
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch all sessions successfully",
                                sessionService.getSessions(page, size, sortBy, sortOrder, params)
                        )
                );
    }

    @PostMapping("/{lotId}")
    @Operation(
            summary = "Create a new parking session",
            description = """
                Create a new parking session when a vehicle enters the parking lot.
                
                **Path Parameters:**
                - `lotId` (required): The unique identifier of the parking lot where the session is being created
                
                **Request Body Fields:**
                - `userId` (optional): ID of the registered user (required for MEMBER sessions, null for OCCASIONAL)
                - `spotId` (required): ID of the parking spot being occupied
                - `vehicleId` (optional): ID of the registered vehicle (required for MEMBER sessions)
                - `licensePlate` (required): License plate number for vehicle identification and verification
                - `vehicleType` (required): Type of vehicle (BIKE, MOTORBIKE, CAR_UP_TO_9_SEATS, OTHER)
                - `authMethod` (required): Authentication method used for entry
                  * BLE - Bluetooth Low Energy proximity detection (mobile app users)
                  * NFC_CARD - Near Field Communication card (occasional users)
                  * QR - QR code scan (reservation-based entry)
                - `sessionType` (required): Type of session
                  * MEMBER - Registered users with linked vehicles and wallets
                  * OCCASIONAL - Walk-in users using NFC cards or cash
                - `referenceType` (required): Access reference type
                  * WALK_IN - Direct entry without prior booking
                  * RESERVATION - Entry based on pre-booked spot
                  * SUBSCRIPTION - Entry using active subscription package
                - `referenceId` (optional): ID reference to reservation or subscription (required if referenceType is not WALK_IN)
                - `entryTime` (required): Timestamp of vehicle entry (ISO 8601 format)
                - `cardUUID` (optional): Required if authMethod is NFC_CARD
                - `pricingRuleId` (required): ID of the pricing rule to apply for fee calculation
                
                **Business Rules:**
                - For MEMBER sessions: userId and vehicleId are required
                - For OCCASIONAL sessions: userId and vehicleId should be null
                - Vehicle must be registered to the user (for MEMBER sessions)
                - Parking spot must be available (status = AVAILABLE)
                - User cannot have multiple active sessions in the same lot simultaneously
                - Entry time cannot be in the future
                - If authMethod is NFC_CARD, cardUUID is mandatory
                - If referenceType is RESERVATION or SUBSCRIPTION, referenceId is required
                - License plate format must match Vietnamese standards (for validation)
                - Pricing rule must be active and valid for the parking lot
                
                **Session Initialization:**
                - Initial status is set to ACTIVE
                - Duration and total amount are initially 0
                - Spot status changes to OCCUPIED
                - Entry timestamp is recorded
                - Sync status is set to PENDING for edge device synchronization
                
                **Returns:** Created session object with:
                - Assigned session UUID
                - Complete session details
                - Initial calculations
                - Status set to ACTIVE
                
                **Error Cases:**
                - 400 Bad Request: Invalid input data or business rule violation
                - 404 Not Found: Parking lot, spot, user, or vehicle not found
                - 409 Conflict: User already has an active session, spot is not available
                """
    )
    public ResponseEntity<?> createSession(
            @Parameter(description = "ID of the parking lot where the vehicle is entering", required = true, example = "1")
            @PathVariable("lotId") Long lotId,

            @Parameter(description = "Session creation request containing all entry details and authentication information", required = true)
            @RequestBody @Valid SessionCreateRequest request
    ) {
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Session created successfully",
                                sessionService.createSession(lotId, request)
                        )
                );
    }

    @PutMapping("/{cardUUID}")
    @Operation(
            summary = "Update parking session (end session or add notes)",
            description = """
                Update an existing parking session, typically to record exit time and complete the session.
                
                **Path Parameters:**
                - `cardUUID` (required): The unique identifier of the access card associated with the session
                
                **Request Body Fields (all optional):**
                - `exitTime`: Timestamp when vehicle exited - setting this will complete the session
                - `note`: Additional notes or comments about the session (max 500 characters)
                
                **Behavior:**
                - If `exitTime` is provided:
                  * Session status changes to COMPLETED
                  * Final parking fee is calculated based on duration and pricing rule
                  * Parking spot becomes available
                  * Payment process may be initiated
                - If only `note` is provided:
                  * Note is added to the session record
                  * Session remains in current status
                
                **Business Rules:**
                - Exit time must be after entry time
                - Exit time cannot be in the future
                - Session must be in ACTIVE status to be completed
                - Cannot update an already completed session
                
                **Use Cases:**
                - Recording vehicle exit and ending the session
                - Adding notes about issues or special circumstances
                - Documenting exceptional cases or incidents
                
                **Returns:** Updated session with exit details and final calculations
                """
    )
    public ResponseEntity<?> updateSession(
            @Parameter(description = "Unique identifier of the access card", required = true, example = "CARD-UUID-123456")
            @PathVariable("cardUUID") String cardUUID,

            @Parameter(description = "Session update request containing exit details or notes", required = true)
            @RequestBody @Valid SessionUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Update session successfully",
                                sessionService.updateSession(cardUUID, request)

                        )
                );
    }

    @GetMapping("/count")
    @Operation(
            summary = "Get total count of parking sessions",
            description = """
                Retrieve the total count of all parking sessions in the system.
                
                **Returns:** Total number of session records including:
                - Active sessions (vehicles currently parked)
                - Completed sessions (historical records)
                - All session types (MEMBER and OCCASIONAL)
                - Sessions across all parking lots
                
                **Use Cases:**
                - Dashboard statistics and KPIs
                - System capacity monitoring
                - Database size estimation
                - General analytics and reporting
                - Quick system health check
                
                **Performance Note:**
                - This endpoint counts all records without filtering
                - For large datasets, consider using filtered counts or caching
                - Response time may vary based on total number of sessions
                
                **Example Response:**
```json
                {
                  "success": true,
                  "message": "Count sessions",
                  "data": 15247
                }
```
                """
    )
    public ResponseEntity<?> countSessions() {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success("Count sessions", sessionService.count())
        );
    }

    @DeleteMapping("/{cardUUID}")
    @Operation(
            summary = "Delete parking session (soft delete)",
            description = """
                Mark a parking session as deleted (soft delete) by its card UUID.
                
                **Path Parameters:**
                - `cardUUID` (required): The unique identifier of the access card associated with the session
                
                **Behavior:**
                - Session status is changed to DELETED (soft delete)
                - Session record remains in database for audit purposes
                - Session no longer appears in normal query results
                - Associated parking spot is released if session was active
                - Historical data is preserved for reporting and reconciliation
                
                **Business Rules:**
                - Only sessions with specific statuses can be deleted (typically not COMPLETED sessions with payments)
                - Cannot delete sessions that are part of financial reconciliation
                - Requires appropriate permissions (admin or partner role)
                - Deletion is logged for audit trail
                
                **Use Cases:**
                - Removing erroneous or test sessions
                - Handling data entry errors
                - Cleaning up incomplete sessions
                - Administrative corrections
                
                **Important Notes:**
                - This is a soft delete - data is not physically removed
                - For complete removal, use hard delete endpoint (if available)
                - Deleted sessions can potentially be restored by administrators
                - Consider business implications before deleting sessions with financial transactions
                
                **Authorization:**
                - Requires ADMIN or PARTNER_OWNER role
                - Partners can only delete sessions from their own parking lots
                
                **Returns:** Success confirmation message
                
                **Error Cases:**
                - 404 Not Found: Session with specified cardUUID does not exist
                - 403 Forbidden: Insufficient permissions to delete session
                - 409 Conflict: Session cannot be deleted due to business rules
                """
    )
    public ResponseEntity<?> deleteSession(
            @Parameter(description = "Unique identifier of the access card for the session to delete", required = true, example = "CARD-UUID-123456")
            @PathVariable("cardUUID") String cardUUID
    ) {
        sessionService.deleteSession(cardUUID);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        "Session deleted successfully"
                )
        );
    }

    @PostMapping("/{lotId}/sync")
    @Operation(
            summary = "Bulk synchronize sessions from edge devices",
            description = """
                Synchronize multiple parking sessions from edge computing units to the central system.
                This endpoint is designed for batch processing of session data collected offline by edge devices.
                
                **Path Parameters:**
                - `lotId` (required): The unique identifier of the parking lot where sessions occurred
                
                **Request Body:**
                - Array of `SessionSyncRequest` objects containing session data from edge devices
                - Each request should include complete session information as recorded by the edge device
                
                **Session Sync Request Fields:**
                - `cardUUID`: Unique identifier of the access card
                - `licensePlate`: Vehicle license plate number
                - `vehicleType`: Type of vehicle
                - `entryTime`: Recorded entry timestamp
                - `exitTime`: Recorded exit timestamp (null if session is still active)
                - `authMethod`: Authentication method used
                - `sessionType`: MEMBER or OCCASIONAL
                - `pricingRuleId`: Pricing rule applied
                - `syncedFromLocal`: Timestamp when edge device recorded this data
                - Additional fields as needed for complete session data
                
                **Synchronization Logic:**
                - **Create new sessions** if they don't exist in central database
                - **Update existing sessions** if found (match by cardUUID or other unique identifier)
                - **Conflict resolution**: Edge device timestamp takes precedence for entry/exit times
                - **Validation**: Each session is validated against business rules
                - **Transaction handling**: All sessions are processed in a single transaction
                - **Rollback on error**: If any session fails validation, entire batch is rolled back
                
                **Business Rules:**
                - Edge device must be registered and authorized for the parking lot
                - Session timestamps must be within reasonable range (not too far in past/future)
                - License plates must be valid format
                - Pricing rules must exist and be active
                - Parking spots must exist in the specified lot
                - Duplicate sessions are handled intelligently (latest data wins)
                
                **Sync Status Tracking:**
                - Each synced session is marked with `sync_status = SYNCED`
                - Original `synced_from_local` timestamp is preserved
                - Sync conflicts are logged for manual review
                
                **Use Cases:**
                - Periodic synchronization from edge devices with limited connectivity
                - Recovery from network outages (edge devices buffer data locally)
                - Reconciliation between edge and central systems
                - Initial data migration when onboarding new parking lots
                - Bulk import of historical session data
                
                **Edge Device Integration:**
                - Edge devices (Raspberry Pi, etc.) collect session data locally
                - Data is buffered when cloud connectivity is unavailable
                - Automatic sync when connectivity is restored
                - Supports up to 24 hours of offline operation
                - MQTT or HTTP can be used for sync communication
                
                **Performance Considerations:**
                - Batch size should be limited (recommended: 100-500 sessions per request)
                - Large batches may timeout - use pagination for bigger syncs
                - Processing time scales with batch size
                - Consider rate limiting for edge device requests
                
                **Returns:** Sync result summary containing:
                - Total number of sessions processed
                - Number of sessions created
                - Number of sessions updated
                - Number of sessions failed with error details
                - List of conflict warnings or validation errors
                
                **Response Example:**
```json
                {
                  "success": true,
                  "message": "Sync sessions successfully",
                  "data": {
                    "totalProcessed": 150,
                    "created": 120,
                    "updated": 25,
                    "failed": 5,
                    "conflicts": [
                      {
                        "cardUUID": "CARD-001",
                        "reason": "Entry time before lot opening hours"
                      }
                    ]
                  }
                }
```
                
                **Error Handling:**
                - 400 Bad Request: Invalid session data or validation failures
                - 404 Not Found: Parking lot not found
                - 409 Conflict: Irreconcilable data conflicts
                - 500 Internal Server Error: Database or processing errors
                
                **Security:**
                - Requires EDGE_DEVICE authentication token
                - Edge device must be registered to the specific parking lot
                - Rate limiting applied per edge device
                - All sync operations are logged for audit
                
                **Best Practices:**
                - Sync frequently (every 5-15 minutes) when connectivity is available
                - Include sync timestamps for troubleshooting
                - Handle partial failures gracefully
                - Monitor sync success rates
                - Alert on repeated sync failures
                """
    )
    public ResponseEntity<?> syncSessions(
            @Parameter(description = "ID of the parking lot to synchronize sessions for", required = true, example = "1")
            @PathVariable("lotId") Long lotId,

            @Parameter(description = "List of session data to synchronize from edge devices", required = true)
            @RequestBody List<SessionSyncRequest> requests
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        "Sync sessions successfully",
                        sessionService.syncSessions(lotId, requests)
                )
        );
    }
}
package com.parkmate.session;

import com.parkmate.common.ApiResponse;
import com.parkmate.session.dto.req.SessionCreateRequest;
import com.parkmate.session.dto.req.SessionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-service/sessions")
@RequiredArgsConstructor
@Tag(name = "Parking Session API", description = "API for managing parking sessions and vehicle entry/exit tracking")
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
            summary = "Get all parking sessions with pagination",
            description = """
                Retrieve a paginated list of all parking sessions with sorting capabilities.
                
                **Query Parameters:**
                - `page` (optional): Page number (default: 0)
                - `size` (optional): Page size (default: 10)
                - `sortBy` (optional): Sort field (default: id) - Available: id, entryTime, exitTime, userId, status
                - `sortOrder` (optional): Sort direction ASC/DESC (default: ASC)
                
                **Returns:** Paginated list of parking sessions including:
                - Session ID and status
                - User and vehicle details
                - Entry and exit times
                - Parking location information
                - Duration and pricing details
                - Authentication method
                
                **Use Cases:**
                - Monitoring active parking sessions
                - Reviewing historical parking data
                - Generating reports and analytics
                - Auditing parking lot usage
                """
    )
    public ResponseEntity<?> getAllSessions(
            @Parameter(description = "Page number (zero-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field name to sort by", example = "id")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch all sessions successfully",
                                sessionService.getSessions(page, size, sortBy, sortOrder)
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
                - `userId` (required): ID of the user parking the vehicle
                - `spotId` (required): ID of the parking spot being occupied
                - `vehicleId` (required): ID of the vehicle entering
                - `licensePlate` (required): License plate number for verification
                - `authMethod` (required): Authentication method used for entry
                  * CARD - Physical access card
                  * QR_CODE - QR code scan
                  * LICENSE_PLATE - License plate recognition
                  * MOBILE_APP - Mobile application
                  * MANUAL - Manual entry by staff
                - `entryTime` (required): Timestamp of vehicle entry
                - `cardUUID` (optional): Required if authMethod is CARD
                - `pricingRuleId` (required): ID of the pricing rule to apply
                
                **Business Rules:**
                - User must have an active account
                - Vehicle must be registered to the user
                - Parking spot must be available
                - User cannot have multiple active sessions simultaneously
                - Entry time cannot be in the future
                - If using CARD auth method, cardUUID is required
                
                **Returns:** Created session with assigned ID and initial status (ACTIVE)
                """
    )
    public ResponseEntity<?> createSession(
            @Parameter(description = "ID of the parking lot", required = true, example = "1")
            @PathVariable("lotId") Long lotId,

            @Parameter(description = "Session creation request containing entry details", required = true)
            @RequestBody @Valid SessionCreateRequest request
    ) {
        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
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

            @Parameter(description = "Session update request containing exit details", required = true)
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
}
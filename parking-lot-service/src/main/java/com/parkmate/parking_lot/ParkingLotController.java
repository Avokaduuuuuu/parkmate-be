package com.parkmate.parking_lot;

import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-service/lots")
@RequiredArgsConstructor
@Tag(name = "Parking Lot API", description = "API requests for Parking Lot")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping
    @Operation(
            summary = "Get all parking lots with filtering and pagination",
            description = """
                Search and retrieve parking lots with filtering and pagination support.
                
                **Query Parameters:**
                - `ownedByMe` (optional): Returns parking lots owned by current authenticated partner
                - `name` (optional): Search by parking lot name (partial match, case-insensitive)
                - `city` (optional): Search by city name (partial match, case-insensitive)
                - `is24Hour` (optional): Filter by 24-hour operation (true/false)
                - `status` (optional): Filter by parking lot status
                  * PENDING - Initial state, just created
                  * UNDER_SURVEY - Being surveyed and evaluated
                  * PREPARING - Infrastructure preparation phase
                  * PARTNER_CONFIGURATION - Partner configuring operations
                  * ACTIVE_PENDING - Awaiting admin approval
                  * ACTIVE - Fully operational
                  * INACTIVE - Temporarily closed
                  * UNDER_MAINTENANCE - Under repairs
                  * MAP_DENIED - Partner denied the created Map
                  * REJECTED - Application rejected
                  * DENIED - Access denied
                - `page` (optional): Page number (default: 0)
                - `size` (optional): Page size (default: 10)
                - `sortBy` (optional): Sort field (default: id) - Available: id, name, city, totalFloors, status, createdAt, updatedAt
                - `sortOrder` (optional): Sort direction ASC/DESC (default: ASC)
                
                **Returns:** Paginated list of parking lots matching the filter criteria
                """
    )
    public ResponseEntity<?> findAll(
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,
            ParkingLotFilterParams params
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "All parking lots found!",
                                parkingLotService.fetchAllParkingLots(
                                        userIdHeader,
                                        page, size, sortBy, sortOrder, params
                                ))
                );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get parking lot by ID",
            description = """
                Retrieve detailed information of a specific parking lot by its ID.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking lot
                
                **Returns:** Complete parking lot details including:
                - Basic information (name, address, location coordinates)
                - Operating hours and 24-hour status
                - Total floors and capacity
                - Current status
                - Associated lot capacities by vehicle type
                - Pricing rules configuration
                """
    )
    public ResponseEntity<?> findById(
            @Parameter(description = "ID of the parking lot to retrieve", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch parking lot by id successfully",
                                parkingLotService.getParkingLotById(id)
                        )
                );
    }

    @PostMapping
    @Operation(

            summary = "Create a new parking lot",
            description = """
                Create a new parking lot for a specific partner with complete configuration.
                
                **Path Parameters:**
                - `partnerId` (required): The unique identifier of the partner who owns this parking lot
                
                **Request Body Fields:**
                - `name` (required): Name of the parking lot (max 255 characters)
                - `streetAddress` (required): Street address (max 255 characters)
                - `ward` (required): Ward or district (max 100 characters)
                - `city` (required): City name (max 100 characters)
                - `latitude` (required): Latitude coordinate (-90 to 90)
                - `longitude` (required): Longitude coordinate (-180 to 180)
                - `totalFloors` (required): Total number of floors (minimum 1)
                - `operatingHoursStart` (required): Opening time in HH:mm:ss format (e.g., "06:00:00")
                - `operatingHoursEnd` (required): Closing time in HH:mm:ss format (e.g., "23:00:00")
                - `is24Hour` (required): Boolean indicating 24-hour operation
                - `lotCapacityRequests` (optional): List of capacity configurations per floor and vehicle type
                - `pricingRuleCreateRequests` (optional): List of pricing rules for different vehicle types and time periods
                
                **Business Rules:**
                - Operating hours start and end times must be different
                - Initial status will be set to PENDING
                - Partner must exist in the system
                
                **Returns:** Created parking lot with assigned ID and initial status
                """
    )
    public ResponseEntity<?> addParkingLot(
            @Parameter(description = "ID of the partner creating this parking lot", required = true, example = "123")
            @RequestBody @Valid ParkingLotCreateRequest request,
            @Parameter(hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Parking Lot created successfully",
                                parkingLotService.addParkingLot(userIdHeader,request)
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing parking lot",
            description = """
                Update parking lot information. All fields are optional - only include fields you want to update.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking lot to update
                
                **Request Body Fields (all optional):**
                - `name`: Name of the parking lot (max 255 characters)
                - `streetAddress`: Street address (max 255 characters)
                - `ward`: Ward or district (max 100 characters)
                - `city`: City name (max 100 characters)
                - `latitude`: Latitude coordinate (-90 to 90)
                - `longitude`: Longitude coordinate (-180 to 180)
                - `totalFloors`: Total number of floors (minimum 1)
                - `operatingHoursStart`: Opening time in HH:mm:ss format (e.g., "06:00:00")
                - `operatingHoursEnd`: Closing time in HH:mm:ss format (e.g., "23:00:00")
                - `is24Hour`: Boolean indicating 24-hour operation
                - `status`: Update parking lot status - see status descriptions below
                - `reason`: Required when status is REJECTED or MAP_DENIED - explanation for the status change
                
                **Status Update Options:**
                - **UNDER_SURVEY** - Set when parking lot needs evaluation
                - **PREPARING** - Set when infrastructure is being prepared
                - **REJECTED** - Set by admin when application is rejected (requires `reason`)
                - **PARTNER_CONFIGURATION** - Set when partner is configuring operations
                - **ACTIVE_PENDING** - Set when configuration complete, awaiting approval
                - **ACTIVE** - Set by admin to make parking lot operational
                - **INACTIVE** - Set when temporarily closing parking lot
                - **UNDER_MAINTENANCE** - Set when parking lot needs maintenance
                - **MAP_DENIED** - Set by admin when location is denied (requires `reason`)
                
                **Important Notes:**
                - Not all status transitions are allowed - follow the business workflow
                - When setting status to REJECTED or MAP_DENIED, the `reason` field is required
                - Only admins can set certain statuses (ACTIVE, REJECTED, MAP_DENIED)
                - Partners can only update operational details and limited status changes
                
                **Returns:** Updated parking lot information
                """
    )
    public ResponseEntity<?> updateParkingLot(
            @Parameter(description = "ID of the parking lot to update", required = true, example = "1")
            @PathVariable("id") Long id,
            @RequestBody @Valid ParkingLotUpdateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Parking Lot updated successfully",
                                parkingLotService.updateParkingLot(id, request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete (deactivate) a parking lot",
            description = """
                Soft delete a parking lot by setting its status to INACTIVE.
                This operation does not permanently remove the parking lot from the database.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking lot to deactivate
                
                **Behavior:**
                - Changes parking lot status to INACTIVE
                - Preserves all historical data and configurations
                - Parking lot will no longer accept new bookings
                - Existing active bookings may need to be handled separately
                - Can be reactivated later by updating status back to ACTIVE
                
                **Use Cases:**
                - Temporarily closing a parking lot
                - Removing a parking lot from active listings
                - Seasonal closures
                
                **Returns:** Confirmation message with updated parking lot status
                """
    )
    public ResponseEntity<?> deleteParkingLot(
            @Parameter(description = "ID of the parking lot to deactivate", required = true, example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Parking Lot Inactive",
                                parkingLotService.deleteParkingLot(id)
                        )
                );
    }
}
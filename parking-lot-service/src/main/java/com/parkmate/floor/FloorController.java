package com.parkmate.floor;

import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-service/floors")
@RequiredArgsConstructor
@Tag(name = "Parking Floor API", description = "API for making and configuring Parking Floor")
@Validated
public class FloorController {

    private final FloorService floorService;


    @Operation(
            summary = "Get all parking floors with filtering and pagination",
            description = """
                Retrieve a paginated list of parking floors with optional filtering and sorting capabilities.
                
                **Query Parameters:**
                - `parkingLotId` (optional): Filter floors by specific parking lot ID
                - `floorNumber` (optional): Filter by floor number (-100 to 100, negative for basement levels)
                - `floorName` (optional): Search by floor name (partial match, case-insensitive)
                - `isActive` (optional): Filter by active status (true/false)
                - `page` (optional): Page number (default: 0)
                - `size` (optional): Page size (default: 10)
                - `sortBy` (optional): Sort field (default: id) - Available: id, floorName, floorNumber, createdAt
                - `sortOrder` (optional): Sort direction ASC/DESC (default: ASC)
                
                **Returns:** Paginated list of parking floors with their capacity information and associated parking lot details
                """
    )
    @GetMapping
    public ResponseEntity<?> findAll(
            @Parameter(description = "Page number (zero-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field name to sort by (e.g., id, floorName, floorNumber)", example = "id")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,

            @Parameter(description = "Filter parameters for floors")
            FloorFilterParams params
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch floors successfully",
                                floorService.findAll(
                                        page, size, sortBy, sortOrder, params
                                )
                        )
                );
    }


    @Operation(
            summary = "Get parking floor by ID",
            description = """
                Retrieve detailed information about a specific parking floor by its unique identifier.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking floor
                
                **Returns:** Complete floor details including:
                - Floor number and display name
                - Associated parking lot information
                - Capacity configurations for each vehicle type
                - Electric vehicle support status
                - Active status and availability
                - Creation and update timestamps
                """
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> findParkingFloorById(
            @Parameter(description = "Unique identifier of the parking floor", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                floorService.getFloorById(id)
                        )
                );
    }

    @Operation(
            summary = "Create a new parking floor",
            description = """
                Add a new floor to an existing parking lot with capacity configuration.
                
                **Path Parameters:**
                - `parkingLotId` (required): The unique identifier of the parking lot where the floor will be created
                
                **Request Body Fields:**
                - `floorNumber` (required): Floor number (-100 to 100)
                  * Negative numbers: Basement levels (e.g., -1 for B1, -2 for B2)
                  * 0: Ground floor
                  * Positive numbers: Upper floors (e.g., 1, 2, 3)
                - `floorName` (required): Display name of the floor (max 100 characters, e.g., "Ground Floor", "Basement 1")
                - `capacityRequests` (optional): List of capacity configurations per vehicle type
                  * Each item includes: capacity, vehicleType, supportElectricVehicle
                  * Vehicle types: CAR_4_SEATS, CAR_7_SEATS, CAR_16_SEATS, MOTORBIKE, BICYCLE, TRUCK, OTHER
                
                **Business Rules:**
                - Floor number must be unique within the parking lot
                - Floor number cannot exceed the parking lot's total floors
                - At least one capacity configuration is recommended
                - Electric vehicle support can be enabled per vehicle type
                
                **Returns:** Created floor with assigned ID and capacity details
                """
    )
    @PostMapping("/{parkingLotId}")
    public ResponseEntity<?> createFloor(
            @Parameter(description = "Unique identifier of the parking lot where the floor will be created", required = true, example = "1")
            @PathVariable("parkingLotId") Long id,

            @Parameter(description = "Floor creation request containing floor details", required = true)
            @RequestBody @Valid FloorCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                floorService.createFloor(id, request)
                        )
                );
    }

    @Operation(
            summary = "Update parking floor information",
            description = """
                Update an existing parking floor's information. All fields are optional - only include fields you want to update.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking floor to update
                
                **Request Body Fields (all optional):**
                - `floorNumber`: Update floor number (-100 to 100)
                  * Must remain unique within the parking lot
                  * Cannot exceed parking lot's total floors
                - `floorName`: Update display name (max 100 characters)
                
                **Important Notes:**
                - Floor number changes require careful consideration if there are existing parking sessions
                - Capacity configurations are managed through separate capacity endpoints
                - Cannot change floor to inactive status through this endpoint (use delete endpoint)
                - Floor must belong to an active parking lot
                
                **Returns:** Updated floor information with new values
                """
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateParkingFloor(
            @Parameter(description = "Unique identifier of the parking floor to update", required = true, example = "1")
            @PathVariable("id") Long id,

            @Parameter(description = "Floor update request containing updated floor details", required = true)
            @RequestBody @Valid FloorUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                floorService.updateFloor(id, request)
                        )
                );
    }

    @Operation(
            summary = "Delete (deactivate) parking floor",
            description = """
                Soft delete a parking floor by setting its status to inactive.
                This operation does not permanently remove the floor from the database.
                
                **Path Parameters:**
                - `id` (required): The unique identifier of the parking floor to delete
                
                **Behavior:**
                - Changes floor status to inactive
                - Preserves all historical data and capacity configurations
                - Floor will no longer be available for new parking sessions
                - Existing parking sessions on this floor are not affected
                
                **Restrictions:**
                - Cannot delete floors with active parking sessions
                - Cannot delete floors with active parking areas
                - Cannot delete if it's the only floor in the parking lot
                - Floor must not have pending or in-progress bookings
                
                **Use Cases:**
                - Temporarily closing a floor for maintenance
                - Removing a floor from active rotation
                - Seasonal or event-based floor closures
                
                **Returns:** Confirmation message with updated floor status
                """
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParkingFloor(
            @Parameter(description = "Unique identifier of the parking floor to delete", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                floorService.deleteFloor(id)
                        )
                );
    }

    @GetMapping("/count")
    public ResponseEntity<?> count() {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success("Count Floors", floorService.count())
        );
    }
}
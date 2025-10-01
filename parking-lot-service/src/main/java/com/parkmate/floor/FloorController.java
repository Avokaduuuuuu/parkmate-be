package com.parkmate.floor;

import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/floors")
@RequiredArgsConstructor
@Tag(name = "Parking Floor API", description = "API for making and configuring Parking Floor")
@Validated
public class FloorController {

    private final FloorService floorService;


    @Operation(
            summary = "Get all parking floors",
            description = "Retrieve a paginated list of parking floors with optional filtering and sorting"
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
            description = "Retrieve detailed information about a specific parking floor by its unique identifier"
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
            description = "Add a new floor to an existing parking lot. The floor will include capacity information for different vehicle types and a unique floor number within the parking lot."
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
            summary = "Update parking floor",
            description = "Update an existing parking floor's information including name, capacity, and active status. The floor number cannot be changed after creation."
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
            summary = "Delete parking floor",
            description = "Soft delete a parking floor by its ID. This will mark the floor as inactive but preserve historical data. Cannot delete floors with active parking sessions or areas."
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
}

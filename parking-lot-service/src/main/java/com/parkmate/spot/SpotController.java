package com.parkmate.spot;

import com.parkmate.common.ApiResponse;
import com.parkmate.spot.dto.req.SpotCreateRequest;
import com.parkmate.spot.dto.req.SpotUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spots")
@RequiredArgsConstructor
@Tag(name = "Spot API", description = "APIs for managing individual parking spots within areas")
public class SpotController {

    private final SpotService spotService;

    @Operation(
            summary = "Get all parking spots",
            description = "Retrieve a list of parking spots with optional filtering by area, status, and spot number. Returns all spots matching the filter criteria."
    )
    @GetMapping
    public ResponseEntity<?> findAll(
            @Parameter(description = "Filter parameters for spots (areaId, status, spotNumber)")
            SpotFilterParams params
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch spots successfully",
                                spotService.findAll(params)
                        )
                );
    }

    @Operation(
            summary = "Get parking spot by ID",
            description = "Retrieve detailed information about a specific parking spot by its unique identifier, including current status, dimensions, and location coordinates"
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable("id")
            @Parameter(description = "Unique identifier of the parking spot", required = true, example = "1")
            @Positive(message = "Spot ID must be positive")
            Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch a spot successfully",
                                spotService.findById(id)
                        )
                );
    }

    @Operation(
            summary = "Add parking spots to an area",
            description = "Add multiple parking spots to an existing area. Each spot must have valid dimensions appropriate for the area's vehicle type, must be within area boundaries, and must not overlap with existing spots. The spot dimensions will be validated against minimum requirements for the area's vehicle type (e.g., motorbike spots: 1.0m x 2.0m minimum, car spots: 2.5m x 5.0m minimum)."
    )
    @PostMapping("/{areaId}")
    public ResponseEntity<?> addSpots(
            @PathVariable("areaId")
            @Parameter(description = "Unique identifier of the area where spots will be added", required = true, example = "1")
            @Positive(message = "Area ID must be positive")
            Long areaId,

            @RequestBody
            @Valid
            @Parameter(description = "List of spot creation requests with coordinates, dimensions, and spot numbers", required = true)
            List<SpotCreateRequest> requests
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Spots created successfully",
                                spotService.addSpots(requests, areaId)
                        )
                );
    }

    @Operation(
            summary = "Update parking spot",
            description = "Update an existing parking spot's information including dimensions, coordinates, status, or spot number. Cannot update the spot if it has an active parking session."
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSpot(
            @PathVariable("id")
            @Parameter(description = "Unique identifier of the spot to update", required = true, example = "1")
            @Positive(message = "Spot ID must be positive")
            Long id,

            @RequestBody
            @Valid
            @Parameter(description = "Spot update request containing updated spot details", required = true)
            SpotUpdateRequest updateRequest
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Spot updated successfully",
                                spotService.updateSpot(id, updateRequest)
                        )
                );
    }

    @Operation(
            summary = "Delete parking spot",
            description = "Soft delete a parking spot by its ID. This marks the spot as disabled/inactive but preserves historical parking session data. Cannot delete spots with active parking sessions. The spot will no longer be available for new parking but existing records remain intact."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpot(
            @PathVariable("id")
            @Parameter(description = "Unique identifier of the spot to delete", required = true, example = "1")
            @Positive(message = "Spot ID must be positive")
            Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Spot soft deleted successfully",
                                spotService.deleteSpot(id)
                        )
                );
    }
}
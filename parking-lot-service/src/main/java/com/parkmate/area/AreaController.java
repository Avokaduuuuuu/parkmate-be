package com.parkmate.area;

import com.parkmate.area.dto.req.AreaCreateRequest;
import com.parkmate.area.dto.req.AreaUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-service/areas")
@RequiredArgsConstructor
@Tag(name = "Area API", description = "API for making and configuring Area")
public class AreaController {

    private final AreaService areaService;

    @Operation(
            summary = "Get all parking areas",
            description = "Retrieve a paginated list of parking areas with optional filtering by floor, name, vehicle type, active status, and electric vehicle support. Supports sorting by various fields."
    )
    @GetMapping
    public ResponseEntity<?> findAll(
            @Parameter(description = "Page number (zero-based index)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field name to sort by (e.g., id, name, vehicleType)", example = "id")
            @RequestParam(required = false, defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,

            @Parameter(description = "Filter parameters for areas")
            AreaFilterParams params
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch Areas successfully",
                                areaService.findAllAreas(page, size, sortBy, sortOrder, params)
                        )
                );
    }

    @Operation(
            summary = "Get parking area by ID",
            description = "Retrieve detailed information about a specific parking area by its unique identifier, including all associated parking spots"
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable("id")
            @Parameter(description = "Unique identifier of the parking area", required = true, example = "1")
            @Positive(message = "Area ID must be positive")
            Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch Area by Id successfully",
                                areaService.findAreaById(id)
                        )
                );
    }

    @Operation(
            summary = "Create a new parking area",
            description = "Add a new parking area to an existing floor with individual parking spots. The area includes vehicle type restrictions, capacity information, and coordinate-based positioning for visual mapping."
    )
    @PostMapping("/{floorId}")
    public ResponseEntity<?> createArea(
            @PathVariable("floorId")
            @Parameter(description = "Unique identifier of the floor where the area will be created", required = true, example = "1")
            @Positive(message = "Floor ID must be positive")
            Long floorId,

            @RequestBody
            @Valid
            @Parameter(description = "Area creation request containing area details and list of spot details", required = true)
            AreaCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Area created successfully",
                                areaService.createArea(request, floorId)
                        )
                );
    }

    @Operation(
            summary = "Update parking area",
            description = "Update an existing parking area's information including name, dimensions, vehicle type restrictions, active status, and electric vehicle support. Individual parking spots are not updated through this endpoint."
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArea(
            @PathVariable("id")
            @Parameter(description = "Unique identifier of the area to update", required = true, example = "1")
            @Positive(message = "Area ID must be positive")
            Long id,

            @RequestBody
            @Valid
            @Parameter(description = "Area update request containing updated area details", required = true)
            AreaUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Area updated successfully",
                                areaService.updateArea(request, id)
                        )
                );
    }

    @Operation(
            summary = "Delete parking area",
            description = "Soft delete a parking area by its ID. This marks the area and all its spots as inactive but preserves historical data. Cannot delete areas with active parking sessions."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArea(
            @PathVariable("id")
            @Parameter(description = "Unique identifier of the area to delete", required = true, example = "1")
            @Positive(message = "Area ID must be positive")
            Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Area deleted successfully",
                                areaService.deleteArea(id)
                        )
                );
    }

    @GetMapping("/count")
    public ResponseEntity<?> count() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Count Areas",
                                areaService.count()
                        )
                );
    }
}

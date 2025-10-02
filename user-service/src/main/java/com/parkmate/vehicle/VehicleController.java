package com.parkmate.vehicle;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
import com.parkmate.vehicle.dto.VehicleSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-service/vehicle")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "APIs for managing user vehicles")
public class VehicleController {

    private final VehicleServiceImpl vehicleService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Vehicle by ID",
            description = """
                    Retrieve detailed information about a specific vehicle.
                    
                    **Parameters:**
                    - `id` (path): Vehicle Long
                    
                    **Returns:** Vehicle details including license plate, type, and owner information
                    """
    )
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long id) {
        VehicleResponse response = vehicleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle fetched successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Get All Vehicles with Pagination",
            description = """
                    Retrieve a paginated list of all vehicles in the system.
                    
                    **Query Parameters:**
                    - `page` (optional): Page number (default: 0)
                    - `size` (optional): Page size (default: 10)
                    - `sort` (optional): Sort field and direction (e.g., "licensePlate,asc")
                    
                    **Returns:** Paginated list of vehicles
                    """
    )
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @ModelAttribute VehicleSearchCriteria searchCriteria
    ) {
        Page<VehicleResponse> vehicleResponse = vehicleService.findAll(page, size, sortBy, sortOrder, searchCriteria);
        return ResponseEntity.ok(ApiResponse.success("Vehicles fetched successfully", vehicleResponse));

    }


    @PostMapping
    @Operation(
            summary = "Add New Vehicle",
            description = """
                    Register a new vehicle for a user.
                    
                    **Request Body (CreateVehicleRequest):**
                    - `licensePlate` (required): Vehicle license plate number
                    - `vehicleType` (required): Type of vehicle (CAR, MOTORBIKE, etc.)
                    - `userId` (required): Owner user ID
                    - Additional vehicle details as needed
                    
                    **Returns:** Created vehicle information
                    """
    )
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(@RequestBody CreateVehicleRequest req) {
        VehicleResponse vehicleResponse = vehicleService.createVehicle(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created successfully", vehicleResponse));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Vehicle",
            description = """
                    Update existing vehicle information.
                    
                    **Parameters:**
                    - `id` (path): Vehicle Long to update
                    
                    **Request Body (UpdateVehicleRequest):**
                    - Fields to update (licensePlate, vehicleType, etc.)
                    
                    **Returns:** Updated vehicle information
                    """
    )
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long id,
            @RequestBody UpdateVehicleRequest req) {
        VehicleResponse vehicleResponse = vehicleService.updateVehicle(id, req);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", vehicleResponse));

    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Vehicle",
            description = """
                    Remove a vehicle from the system.
                    
                    **Parameters:**
                    - `id` (path): Vehicle Long to delete
                    
                    **Returns:** Success message
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully"));
    }


}

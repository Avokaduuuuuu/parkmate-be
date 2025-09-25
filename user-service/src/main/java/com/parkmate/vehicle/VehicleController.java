package com.parkmate.vehicle;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-service/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleServiceImpl vehicleService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable UUID id) {
        VehicleResponse response = vehicleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        Page<VehicleResponse> vehicleResponse = vehicleService.findAll(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Vehicles fetched successfully", vehicleResponse));

    }


    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(@RequestBody CreateVehicleRequest req) {
        VehicleResponse vehicleResponse = vehicleService.createVehicle(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created successfully", vehicleResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable UUID id,
            @RequestBody UpdateVehicleRequest req) {
        VehicleResponse vehicleResponse = vehicleService.updateVehicle(id, req);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", vehicleResponse));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }


}

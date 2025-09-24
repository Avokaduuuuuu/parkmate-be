package com.parkmate.controller;

import com.parkmate.dto.req.ParkingFloorCreateRequest;
import com.parkmate.dto.req.ParkingFloorUpdateRequest;
import com.parkmate.dto.resp.ApiResponse;
import com.parkmate.service.ParkingFloorService;
import com.parkmate.service.ParkingLotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking-lot-service/parking-floors")
@RequiredArgsConstructor
@Tag(name = "Parking Floor API", description = "API for making and configuring Parking Floor")
@Validated
public class ParkingFloorController {

    private final ParkingFloorService parkingFloorService;


    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                parkingFloorService.findAll(
                                        page, size, sortBy, sortOrder
                                )
                        )
                );
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> findParkingFloorById(@PathVariable @Positive Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                parkingFloorService.getFloorById(id)
                        )
                );
    }

    @PostMapping("/{parkingLotId}")
    public ResponseEntity<?> createFloor(
            @PathVariable("parkingLotId") Long id,
            @RequestBody @Valid ParkingFloorCreateRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                parkingFloorService.createFloor(
                                        id,
                                        request
                                )
                        )
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParkingFloor(@PathVariable @Positive Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                parkingFloorService.deleteFloor(id)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParkingFloor(
        @PathVariable("id") Long id,
        @RequestBody @Valid ParkingFloorUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                parkingFloorService.updateFloor(id, request)
                        )
                );
    }
}

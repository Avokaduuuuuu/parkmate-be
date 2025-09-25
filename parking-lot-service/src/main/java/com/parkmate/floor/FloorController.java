package com.parkmate.floor;

import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking-floors")
@RequiredArgsConstructor
@Tag(name = "Parking Floor API", description = "API for making and configuring Parking Floor")
@Validated
public class FloorController {

    private final FloorService floorService;


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
                                floorService.findAll(
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
                                floorService.getFloorById(id)
                        )
                );
    }

    @PostMapping("/{parkingLotId}")
    public ResponseEntity<?> createFloor(
            @PathVariable("parkingLotId") Long id,
            @RequestBody @Valid FloorCreateRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                floorService.createFloor(
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
                                floorService.deleteFloor(id)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParkingFloor(
        @PathVariable("id") Long id,
        @RequestBody @Valid FloorUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                floorService.updateFloor(id, request)
                        )
                );
    }
}

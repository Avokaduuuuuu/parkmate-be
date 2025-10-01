package com.parkmate.parking_lot;

import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking-lots")
@RequiredArgsConstructor
@Tag(name = "Parking Lot API", description = "API requests for Parking Lot")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "All parking lots found!",
                                parkingLotService.fetchAllParkingLots(
                                page, size, sortBy, sortOrder
                        ))
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetch parking lot by id successfully",
                                parkingLotService.getParkingLotById(id)
                        )
                );
    }

    @PostMapping("/{partnerId}")
    public ResponseEntity<?> addParkingLot(
            @PathVariable("partnerId") Long partnerId,
            @RequestBody @Valid ParkingLotCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Parking Lot created successfully",
                                parkingLotService.addParkingLot(partnerId,request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParkingLot(
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
    public ResponseEntity<?> deleteParkingLot(@PathVariable("id") Long id) {
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

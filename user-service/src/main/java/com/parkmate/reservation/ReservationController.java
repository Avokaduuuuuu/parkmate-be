package com.parkmate.reservation;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.reservation.dto.*;
import com.parkmate.vehicle.VehicleType;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-service/reservations")
@Tag(name = "Reservation Management", description = "Endpoints for reservation management")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationHoldService reservationHoldService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @RequestBody CreateReservationRequest createReservationRequest,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.createReservation(createReservationRequest, userIdHeader)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getReservation(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader,
            @ModelAttribute ReservationSearchCriteria criteria) {

        return ResponseEntity.ok(ApiResponse.success(reservationService.getReservations(page, size, sortBy, sortOrder, criteria, userIdHeader)));
    }

    @GetMapping("/overlap")
    public ResponseEntity<ApiResponse<Long>> getOverlap(
            @RequestParam Long parkingLotId,
            @RequestParam LocalDateTime start,
            @RequestParam Integer assumedStayMinute,
            @RequestParam VehicleType vehicleType
    ) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.checkOverlap(parkingLotId, start, assumedStayMinute, vehicleType)));
    }

    @GetMapping("/{lotId}/sync")
    public ResponseEntity<ApiResponse<List<SyncReservationResponse>>> syncReservation(
            @PathVariable Long lotId,
            @RequestParam ReservationStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getReservationForSyncing(lotId, status)));
    }

    @PutMapping("/{id}/sync")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservationFromSyncing(
            @PathVariable Long id,
            @RequestBody SyncReservationUpdateRequest request) {
        reservationService.updateReservation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reservation updated successfully"));
    }

    @GetMapping("/count")
    @Hidden
    public ResponseEntity<ApiResponse<Long>> countReservation(
            @RequestParam Long parkingLotId,
            @RequestParam LocalDateTime start,
            @RequestParam Integer assumedStayMinute,
            @RequestParam VehicleType vehicleType) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.checkOverlap(parkingLotId, start, assumedStayMinute, vehicleType)));
    }

    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<HoldSlotResponse>> holdSlot(
            @RequestBody HoldSlotRequest request,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {

        if (userIdHeader != null && request.getUserId() == null) {
            // If user is authenticated and userId not provided, use the authenticated user
            request.setUserId(Long.parseLong(userIdHeader));
        }

        HoldSlotResponse response = reservationHoldService.holdSlot(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/hold/{holdId}")
    public ResponseEntity<ApiResponse<String>> releaseHold(@PathVariable String holdId) {
        reservationHoldService.releaseHold(holdId);
        return ResponseEntity.ok(ApiResponse.success("Slot released successfully"));
    }

    @GetMapping("/hold/{holdId}")
    public ResponseEntity<ApiResponse<TemporaryReservationHold>> getHold(@PathVariable String holdId) {
        TemporaryReservationHold hold = reservationHoldService.getHold(holdId);
        if (hold == null) {
            return ResponseEntity.ok(ApiResponse.error("HOLD_NOT_FOUND", "Hold not found or expired"));
        }
        return ResponseEntity.ok(ApiResponse.success(hold));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelReservation(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        reservationService.cancelReservation(id, userIdHeader);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled successfully"));
    }


}

package com.parkmate.reservation;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.reservation.dto.CreateReservationRequest;
import com.parkmate.reservation.dto.ReservationResponse;
import com.parkmate.reservation.dto.ReservationSearchCriteria;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-service/reservations")
@Tag(name = "Reservation Management", description = "Endpoints for reservation management")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

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


}

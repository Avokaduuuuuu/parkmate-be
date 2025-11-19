package com.parkmate.reservation;

import com.parkmate.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/user-service/reservations")
@Hidden
@RequiredArgsConstructor
public class InternalReservationController {

    private final ReservationHoldService reservationHoldService;


    @GetMapping("/holds/count")
    public ResponseEntity<ApiResponse<Long>> countTemporaryHolds(
            @RequestParam Long parkingLotId,
            @RequestParam String vehicleType,
            @RequestParam String reservedFrom,
            @RequestParam Integer assumedStayMinute) {

        Long count = reservationHoldService.countTemporaryHolds(
                parkingLotId,
                vehicleType,
                reservedFrom,
                assumedStayMinute
        );

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
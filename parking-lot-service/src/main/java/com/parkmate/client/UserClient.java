package com.parkmate.client;

import com.parkmate.client.response.PlatformPartnerStatistic;
import com.parkmate.client.response.PlatformUserStatistic;
import com.parkmate.client.response.UserServiceStatistic;
import com.parkmate.common.ApiResponse;
import com.parkmate.common.enums.VehicleType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/v1/user-service/reservations/overlap")
    ApiResponse<Map<Long, Boolean>> reservationOverlap(
            @RequestParam List<Long> spotIds,
            @RequestParam String start,
            @RequestParam String end
    );

    @GetMapping("/api/v1/user-service/reservations/count")
    ApiResponse<Long> countReservation(
            @RequestParam Long parkingLotId,
            @RequestParam LocalDateTime start,
            @RequestParam Integer assumedStayMinute,
            @RequestParam VehicleType vehicleType);

    @GetMapping("/internal/user-service/reservations/holds/count")
    ApiResponse<Long> countTemporaryHolds(
            @RequestParam Long parkingLotId,
            @RequestParam String vehicleType,
            @RequestParam String reservedFrom,
            @RequestParam Integer assumedStayMinute);

    @GetMapping("/internal/user-service/user-subscriptions/check-occupied-spots")
    List<Long> checkOccupiedSpots(
            @RequestParam("spotIds") List<Long> spotIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    );

    @GetMapping("/api/v1/user-service/statistics/{lotId}/revenue")
    ApiResponse<UserServiceStatistic> getUserRevenueStatistic(
            @PathVariable("lotId") Long lotId,
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    );

    @PostMapping("/internal/users/{userId}/check-wallet-balance")
    ApiResponse<Boolean> checkWalletBalance(
            @PathVariable("userId") Long userId,
            @RequestParam("sessionId") String sessionId,
            @RequestParam("requiredAmount") BigDecimal requiredAmount
    );

    @GetMapping("/api/v1/user-service/statistics/platform/partner")
    ApiResponse<PlatformPartnerStatistic> getPartnerStatistic();

    @GetMapping("/api/v1/user-service/statistics/platform/user")
    ApiResponse<PlatformUserStatistic> getUserStatistic(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    );

//    @GetMapping("/api/v1/user-service/reservations/count")
//    ApiResponse<Long> countReservation(
//            @RequestParam Long parkingLotId,
//            @RequestParam LocalDateTime start,
//            @RequestParam Integer assumedStayMinute,
//            @RequestParam VehicleType vehicleType,
//            @RequestParam Boolean isElectric);
}

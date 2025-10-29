package com.parkmate.client;

import com.parkmate.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}

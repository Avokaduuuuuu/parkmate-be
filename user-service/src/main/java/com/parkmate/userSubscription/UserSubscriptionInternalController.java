package com.parkmate.userSubscription;

import com.parkmate.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/internal/user-subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscription Internal", description = "Internal APIs for user parking subscriptions")
public class UserSubscriptionInternalController {

    private final UserSubscriptionService userSubscriptionService;


    @GetMapping("/check-occupied-spots")
    public List<Long> checkOccupiedSpot(
            @RequestParam List<Long> spotIds,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        return userSubscriptionService.findOccupiedSpots(spotIds, startDate, endDate);
    }

    @GetMapping("/revenue")
    public ApiResponse<BigDecimal> getSubscriptionRevenue(
            @RequestParam Long lotId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        LocalDateTime fromDate = LocalDateTime.parse(from);
        LocalDateTime toDate = LocalDateTime.parse(to);

        // Sử dụng query có sẵn trong UserSubscriptionRepository
        BigDecimal revenue = userSubscriptionService.getTotalRevenue(
                lotId,
                fromDate,
                toDate
        );

        return ApiResponse.success(revenue != null ? revenue : BigDecimal.ZERO);
    }
}

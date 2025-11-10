package com.parkmate.userSubscription;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/internal/user-service/user-subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscription Management", description = "APIs for managing user parking subscriptions")
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

}

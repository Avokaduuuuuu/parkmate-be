package com.parkmate.client;

import com.parkmate.client.dto.RevenueWithCount;
import com.parkmate.client.dto.UserResponse;
import com.parkmate.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/user-service/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/user-service/users/internal/account/{accountId}")
    Long getUserIdByAccountId(@PathVariable("accountId") Long accountId);

    /**
     * Get partner ID from account ID
     *
     * @param accountId The account ID
     * @return Partner ID
     */
    @GetMapping("/internal/partners/account/{accountId}")
    ApiResponse<Long> getPartnerIdByAccountId(@PathVariable("accountId") Long accountId);

    /**
     * Get total revenue and count from user subscriptions for a parking lot in a date range
     *
     * @param lotId  The parking lot ID
     * @param from   Start date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @param to     End date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @return Revenue and count data
     */
    @GetMapping("/internal/user-subscriptions/revenue")
    ApiResponse<RevenueWithCount> getSubscriptionRevenue(
            @RequestParam("lotId") Long lotId,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    );

    /**
     * Get penalty revenue from cancelled/expired reservations
     * This includes reservations that didn't create sessions but kept the deposit
     *
     * @param lotId  The parking lot ID
     * @param from   Start date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @param to     End date (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @return Penalty revenue and count data
     */
    @GetMapping("/internal/reservations/penalty-revenue")
    ApiResponse<RevenueWithCount> getReservationPenaltyRevenue(
            @RequestParam("lotId") Long lotId,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    );

}
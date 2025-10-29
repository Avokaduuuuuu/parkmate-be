package com.parkmate.userSubscription;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.userSubscription.dto.CreateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UpdateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UserSubscriptionResponse;
import com.parkmate.userSubscription.dto.UserSubscriptionSearchCriteria;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-service/user-subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> createUserSubscription(
            @RequestBody CreateUserSubscriptionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userSubscriptionService.create(request, userIdHeader)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getUserSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserSubscriptionResponse>>> getUserSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader,
            @ModelAttribute UserSubscriptionSearchCriteria searchCriteria
    ) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.findAll(page, size, sortBy, sortOrder, userIdHeader, searchCriteria)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> updateUserSubscription(
            @PathVariable Long id,
            @RequestBody UpdateUserSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userSubscriptionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserSubscription(@PathVariable Long id) {
        userSubscriptionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("User subscription deleted successfully"));
    }
}

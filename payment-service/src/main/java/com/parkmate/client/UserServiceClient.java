package com.parkmate.client;

import com.parkmate.common.ApiResponse;
import com.parkmate.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/user-service/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);
}
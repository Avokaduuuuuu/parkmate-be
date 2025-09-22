package com.parkmate.service;

import com.parkmate.dto.request.CreateUserRequest;
import com.parkmate.dto.response.UserResponse;
import jakarta.validation.constraints.NotBlank;

public interface UserService {

    UserResponse login(@NotBlank String phone, @NotBlank String password);

    UserResponse register(CreateUserRequest request);

}

package com.parkmate.user;

import com.parkmate.user.dto.CreateUserRequest;
import com.parkmate.user.dto.UserResponse;
import jakarta.validation.constraints.NotBlank;

public interface UserService {

    UserResponse login(@NotBlank String phone, @NotBlank String password);

    UserResponse register(CreateUserRequest request);

}

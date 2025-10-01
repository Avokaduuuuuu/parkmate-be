package com.parkmate.auth.dto;

import com.parkmate.user.dto.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    AuthResponse authResponse;
    UserResponse userResponse;

}

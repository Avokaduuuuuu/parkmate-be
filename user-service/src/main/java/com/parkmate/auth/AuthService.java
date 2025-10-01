package com.parkmate.auth;

import com.parkmate.auth.dto.AuthResponse;
import com.parkmate.auth.dto.LoginRequest;
import com.parkmate.auth.dto.LogoutRequest;
import com.parkmate.auth.dto.RefreshRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

}

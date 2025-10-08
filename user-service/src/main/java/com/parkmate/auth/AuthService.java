package com.parkmate.auth;

import com.parkmate.auth.dto.*;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    RegisterResponse register(RegisterRequest request);

    EmailVerificationResponse verifyEmail(String token);

    void resendVerificationEmail(String email);

}

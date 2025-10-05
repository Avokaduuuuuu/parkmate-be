package com.parkmate.auth;

import com.parkmate.auth.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    RegisterResponse register(RegisterRequest request, MultipartFile frontIdImage, MultipartFile backIdImage);

    EmailVerificationResponse verifyEmail(String token);

    void resendVerificationEmail(String email);

}

package com.parkmate.auth;

import com.parkmate.auth.dto.*;
import com.parkmate.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user-service/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs - Login, Logout, Register, Refresh token")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user with email and password. Returns access token and refresh token."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Logout user by invalidating the refresh token"
    )
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generate new access token using refresh token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Customer registration",
            description = "Register a new customer account. Returns user info and auth tokens. A verification email will be sent."
    )
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful. Please verify your email."));
    }

    @PutMapping("/verify")
    @Operation(
            summary = "Verify email with verification code",
            description = "Verify user email using the verification code sent to their email address"
    )
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
            @RequestParam String verifyCode
    ) {
        EmailVerificationResponse response = authService.verifyEmail(verifyCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Email verified successfully"));
    }

    @PutMapping("/resend")
    @Operation(
            summary = "Resend verify email with verification code",
            description = "Resend verify email"
    )
    public ResponseEntity<ApiResponse<?>> resendVerificationEmail(
            @RequestParam String email
    ) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Email resend successfully"));
    }

}

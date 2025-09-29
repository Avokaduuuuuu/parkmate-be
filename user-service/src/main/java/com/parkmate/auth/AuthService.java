package com.parkmate.auth;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.auth.dto.AuthResponse;
import com.parkmate.auth.dto.LoginRequest;
import com.parkmate.auth.dto.LogoutRequest;
import com.parkmate.auth.dto.RefreshRequest;
import com.parkmate.common.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    public AuthResponse login(LoginRequest request) {
        // 1. Find account
        Account account = accountRepository.findAccountByEmail(
                request.email()
        ).orElseThrow(() -> new RuntimeException("Account not found"));

        // 2. Check status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        // 3. Verify password
        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 4. Build JWT claims
        Map<String, Object> claims = buildClaims(account);

        // 5. Generate tokens
        String accessToken = jwtUtil.generateToken(claims);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        // 6. Store refresh token in Redis (30 days)
        redisTokenService.storeRefreshToken(refreshToken, claims, 2592000L);

        // 7. Update last login
        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);

        log.info("User logged in successfully: {}", account.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(172800L) // 48 hours
                .build();
    }

    public AuthResponse refresh(RefreshRequest request) {
        // 1. Get user info from Redis
        Map<String, Object> userInfo = redisTokenService.getUserInfo(request.refreshToken());

        if (userInfo == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 2. Generate new access token
        String newAccessToken = jwtUtil.generateToken(userInfo);

        log.info("Token refreshed for user: {}", userInfo.get("userId"));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.refreshToken()) // Keep same refresh token
                .tokenType("Bearer")
                .expiresIn(172800L)
                .build();
    }

    public void logout(LogoutRequest request) {
        redisTokenService.deleteRefreshToken(request.refreshToken());
        log.info("User logged out successfully");
    }

    private Map<String, Object> buildClaims(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", account.getId());
        claims.put("email", account.getEmail());
        claims.put("username", account.getUsername());
        claims.put("role", account.getRole().toString());
        return claims;
    }
}
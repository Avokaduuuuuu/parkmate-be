package com.parkmate.auth;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.auth.dto.AuthResponse;
import com.parkmate.auth.dto.LoginRequest;
import com.parkmate.auth.dto.LogoutRequest;
import com.parkmate.auth.dto.RefreshRequest;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.email.EmailService;
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
public class AuthServiceImpl implements AuthService {

    public static final String TOKEN_TYPE = "Bearer";
    public static final Long ACCESS_TOKEN_EXPIRATION = 172800L;
    public static final Long REFRESH_TOKEN_EXPIRATION = 2592000L;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final EmailService emailService;

    @Override
    public AuthResponse login(LoginRequest request) {

        Account account = accountRepository.findAccountByEmail(
                request.email()
        ).orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));

        if (account.getStatus() == AccountStatus.DELETED) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE, "Account is not active");
        }

        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH, "Invalid password");
        }

        Map<String, Object> claims = buildClaims(account);

        String accessToken = jwtUtil.generateToken(claims);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        redisTokenService.storeRefreshToken(refreshToken, claims, REFRESH_TOKEN_EXPIRATION);

        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);

        log.info("User logged in successfully: {}", account.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(ACCESS_TOKEN_EXPIRATION) // 48 hours
                .build();
    }

    @Override
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
                .tokenType(TOKEN_TYPE)
                .expiresIn(ACCESS_TOKEN_EXPIRATION)
                .build();
    }

    @Override
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


    @Override
    public void verifyEmail(String token) {
        Account account = accountRepository.findAccountByEmailVerificationToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));

        if (account.getEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        account.setEmailVerified(true);
        account.setEmailVerificationToken(null);

        updateStatusAfterVerification(account);

        accountRepository.save(account);
        log.info("Email verified successfully for: {}", account.getEmail());
    }

    private void updateStatusAfterVerification(Account account) {

        switch (account.getRole()) {
            case MEMBER:
                account.setStatus(AccountStatus.ACTIVE);
                log.info("Member account activated: {}", account.getEmail());
                break;

            case PARTNER_OWNER:
                account.setStatus(AccountStatus.PENDING_APPROVAL);
                log.info("Partner account pending approval: {}", account.getEmail());
                break;

            case ADMIN:
                account.setStatus(AccountStatus.ACTIVE);
                break;

            default:
                account.setStatus(AccountStatus.PENDING_APPROVAL);
        }
    }

    @Override
    public void resendVerificationEmail(String email) {
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        if (account.getEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        String newToken = UUID.randomUUID().toString();
        account.setEmailVerificationToken(newToken);
        accountRepository.save(account);

        try {

            emailService.sendVerificationEmail(
                    account.getEmail(),
                    newToken,
                    account.getRole().equals(AccountRole.MEMBER) ? account.getUser().getFullName() : account.getPartner().getCompanyName()
            );
            log.info("Resent verification email to: {}", email);
        } catch (Exception e) {
            log.error("Failed to resend verification email to: {}", email, e);
            throw new AppException(ErrorCode.EMAIL_RESEND_FAILED, "Failed to resend verification email");
        }
    }


}
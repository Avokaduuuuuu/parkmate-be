package com.parkmate.auth;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.auth.dto.*;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.email.EmailService;
import com.parkmate.s3.S3Service;
import com.parkmate.user.User;
import com.parkmate.user.UserMapper;
import com.parkmate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final S3Service s3Service;

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


    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request, MultipartFile frontIdImage, MultipartFile backIdImage) {
        // Check if account already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // Create verification token
        String verificationToken = generateNumericVerificationToken();

        // Upload ID images to S3 if provided
        String frontPhotoUrl = null;
        String backPhotoUrl = null;

        try {
            if (frontIdImage != null && !frontIdImage.isEmpty()) {
                frontPhotoUrl = s3Service.uploadFile(frontIdImage, "id-cards/front", request.getIdNumber());
            }

            if (backIdImage != null && !backIdImage.isEmpty()) {
                backPhotoUrl = s3Service.uploadFile(backIdImage, "id-cards/back", request.getIdNumber());
            }
        } catch (Exception e) {
            log.error("Failed to upload ID images to S3", e);
            throw new AppException(ErrorCode.S3_UPLOAD_FAILED);
        }

        // Create Account
        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.MEMBER)
                .phone(request.getPhone())
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerificationToken(verificationToken)
                .build();

        Account savedAccount = accountRepository.save(account);

        // Create User linked to Account
        User user = User.builder()
                .account(savedAccount)
                .phone(request.getPhone())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().toLocalDate() : null)
                .address(request.getAddress())
                .idNumber(request.getIdNumber())
                .issuePlace(request.getIssuePlace())
                .issueDate(request.getIssueDate() != null ? request.getIssueDate().toLocalDate() : null)
                .expiryDate(request.getExpiryDate() != null ? request.getExpiryDate().toLocalDate() : null)
                .frontPhotoPath(frontPhotoUrl)
                .backPhotoPath(backPhotoUrl)
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        sendVerificationEmail(savedAccount.getEmail(), verificationToken, savedUser.getFullName());

        // Generate tokens
        Map<String, Object> claims = buildClaims(savedAccount);
        String accessToken = jwtUtil.generateToken(claims);
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        redisTokenService.storeRefreshToken(refreshToken, claims, REFRESH_TOKEN_EXPIRATION);

        return RegisterResponse.builder()
                .authResponse(AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType(TOKEN_TYPE)
                        .expiresIn(ACCESS_TOKEN_EXPIRATION)
                        .build())
                .userResponse(userMapper.toResponse(savedUser))
                .build();
    }

    private String generateNumericVerificationToken() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private Map<String, Object> buildClaims(Account account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", account.getId());
        claims.put("email", account.getEmail());
        claims.put("role", account.getRole().toString());
        return claims;
    }


    @Override
    public EmailVerificationResponse verifyEmail(String token) {
        Account account = accountRepository.findAccountByEmailVerificationToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VALIDATION_CODE));

        if (account.getEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }
        account.setEmailVerified(true);
        account.setEmailVerificationToken(null);
        updateStatusAfterVerification(account);
        accountRepository.save(account);
        log.info("Email verified successfully for: {}", account.getEmail());
        return EmailVerificationResponse.builder()
                .isSuccess(true)
                .message("Email verified for user: " + account.getEmail())
                .build();
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

        String newToken = generateNumericVerificationToken();
        account.setEmailVerificationToken(newToken);
        accountRepository.save(account);

        try {

            if (account.getRole() == AccountRole.PARTNER_OWNER) {
                emailService.sendPartnerVerificationEmail(
                        account.getEmail(),
                        newToken
                );

            } else {
                emailService.sendMemberVerificationEmail(
                        account.getEmail(),
                        newToken,
                        account.getRole().equals(AccountRole.MEMBER) ? account.getUser().getFullName() : account.getPartner().getCompanyName()
                );
            }

            log.info("Resent verification email to: {}", email);
        } catch (Exception e) {
            log.error("Failed to resend verification email to: {}", email, e);
            throw new AppException(ErrorCode.EMAIL_RESEND_FAILED, "Failed to resend verification email");
        }
    }

    private void sendVerificationEmail(String toEmail, String token, String recipientName) {
        try {
            emailService.sendMemberVerificationEmail(
                    toEmail,
                    token,
                    recipientName
            );
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }
}
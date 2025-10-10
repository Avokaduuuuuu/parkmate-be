package com.parkmate.auth;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.account.dto.AccountBasicResponse;
import com.parkmate.auth.dto.*;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.email.EmailService;
import com.parkmate.partner.Partner;
import com.parkmate.partner.PartnerMapper;
import com.parkmate.partner.dto.PartnerResponse;
import com.parkmate.s3.S3Service;
import com.parkmate.user.User;
import com.parkmate.user.UserMapper;
import com.parkmate.user.UserRepository;
import com.parkmate.user.UserService;
import com.parkmate.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PartnerMapper partnerMapper;

    @Override
    public LoginResponse login(LoginRequest request) {

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

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(ACCESS_TOKEN_EXPIRATION)
                .build();

        // Return different response based on role
        switch (account.getRole()) {
            case PARTNER_OWNER:
                Partner partner = account.getPartner();
                if (partner == null) {
                    return LoginResponse.builder()
                            .authResponse(authResponse)
                            .partnerResponse(null)
                            .build();
                }

                PartnerResponse partnerResponse = partnerMapper.toDto(partner);
                String presignedUrl = partner.getBusinessLicenseFileUrl() != null
                        ? s3Service.generatePresignedUrl(partner.getBusinessLicenseFileUrl())
                        : null;

                PartnerResponse partnerWithPresignedUrl = new PartnerResponse(
                        partnerResponse.id(),
                        partnerResponse.companyName(),
                        partnerResponse.taxNumber(),
                        partnerResponse.businessLicenseNumber(),
                        presignedUrl,
                        partnerResponse.companyAddress(),
                        partnerResponse.companyPhone(),
                        partnerResponse.companyEmail(),
                        partnerResponse.businessDescription(),
                        partnerResponse.status(),
                        partnerResponse.suspensionReason(),
                        partnerResponse.createdAt(),
                        partnerResponse.updatedAt(),
                        partnerResponse.accounts()
                );

                return LoginResponse.builder()
                        .authResponse(authResponse)
                        .partnerResponse(partnerWithPresignedUrl)
                        .build();

            case MEMBER:
                User user = account.getUser();
                if (user == null) {
                    throw new AppException(ErrorCode.USER_NOT_FOUND, "User not found for account");
                }

                UserResponse userResponse = responseWithPresignedURL(userMapper.toResponse(user), user);

                return LoginResponse.builder()
                        .authResponse(authResponse)
                        .userResponse(userResponse)
                        .build();

            default:
                // ADMIN, PARTNER_STAFF, and other roles - only return auth response
                return LoginResponse.builder()
                        .authResponse(authResponse)
                        .build();
        }
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
    public RegisterResponse register(RegisterRequest request) {
        // Check if account already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // Create verification token
        String verificationToken = generateNumericVerificationToken();

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
                .userResponse(responseWithPresignedURL(userMapper.toResponse(savedUser), savedUser))
                .build();
    }

    private String generateNumericVerificationToken() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private Map<String, Object> buildClaims(Account account) {
        Map<String, Object> claims = new HashMap<>();
        if (account.getRole() == AccountRole.PARTNER_OWNER && account.getPartner() != null) {
            claims.put("userId", account.getPartner().getId());
        } else {
            claims.put("userId", account.getId());
        }
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
                String fullName = account.getUser().getFirstName() + " " + account.getUser().getLastName();
                emailService.sendMemberVerificationEmail(
                        account.getEmail(),
                        newToken,
                        account.getRole().equals(AccountRole.MEMBER) ? fullName : account.getPartner().getCompanyName()
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

    private UserResponse responseWithPresignedURL(UserResponse response, User user) {
        return getUserResponse(response, user, s3Service);
    }

    @NonNull
    public static UserResponse getUserResponse(UserResponse response, User user, S3Service s3Service) {
        String frontPhotoUrl = user.getFrontPhotoPath() != null
                ? s3Service.generatePresignedUrl(user.getFrontPhotoPath())
                : null;

        String backPhotoUrl = user.getBackPhotoPath() != null
                ? s3Service.generatePresignedUrl(user.getBackPhotoPath())
                : null;

        String profilePictureUrl = user.getProfilePictureUrl() != null
                ? s3Service.generatePresignedUrl(user.getProfilePictureUrl())
                : null;

        AccountBasicResponse accountBasicResponse = new AccountBasicResponse(
                user.getAccount().getId(),
                user.getAccount().getEmail(),
                user.getAccount().getStatus(),
                user.getAccount().getRole()
        );

        return new UserResponse(
                accountBasicResponse,
                response.id(),
                response.phone(),
                response.firstName(),
                response.lastName(),
                response.fullName(),
                response.dateOfBirth(),
                response.address(),
                response.gender(),
                response.nationality(),
                response.profilePictureUrl(),
                response.idNumber(),
                response.issuePlace(),
                response.issueDate(),
                response.expiryDate(),
                frontPhotoUrl,
                backPhotoUrl,
                profilePictureUrl,
                response.createdAt(),
                response.updatedAt()
        );
    }
}
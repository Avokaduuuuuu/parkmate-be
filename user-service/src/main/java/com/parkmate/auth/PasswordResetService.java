package com.parkmate.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final long TOKEN_EXPIRATION_MINUTES = 15;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Store password reset token in Redis with 15-minute TTL
     */
    public void storeResetToken(String email, String token) {
        String key = PASSWORD_RESET_PREFIX + email.toLowerCase();
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.info("Password reset token stored for email: {}", email);
    }

    /**
     * Get reset token by email
     * @return token or null if not found/expired
     */
    public String getResetToken(String email) {
        String key = PASSWORD_RESET_PREFIX + email.toLowerCase();
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Delete reset token after successful password reset
     */
    public void deleteResetToken(String email) {
        String key = PASSWORD_RESET_PREFIX + email.toLowerCase();
        redisTemplate.delete(key);
        log.info("Password reset token deleted for email: {}", email);
    }

    /**
     * Check if the provided token matches the stored token
     */
    public boolean isValidToken(String email, String token) {
        String storedToken = getResetToken(email);
        if (storedToken == null) {
            log.warn("No password reset token found for email: {}", email);
            return false;
        }
        return storedToken.equals(token);
    }

    /**
     * Check if a reset token exists for the email (not expired)
     */
    public boolean hasActiveToken(String email) {
        return getResetToken(email) != null;
    }

    /**
     * Get token expiration time in minutes
     */
    public long getTokenExpirationMinutes() {
        return TOKEN_EXPIRATION_MINUTES;
    }
}

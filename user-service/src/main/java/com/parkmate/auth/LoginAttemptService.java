package com.parkmate.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String ACCOUNT_LOCKED_PREFIX = "account_locked:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked(Long accountId) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + accountId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * Get remaining lock time in minutes
     */
    public long getRemainingLockTimeMinutes(Long accountId) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + accountId;
        Long remainingSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        if (remainingSeconds == null || remainingSeconds < 0) {
            return 0;
        }
        return (remainingSeconds / 60) + 1; // Round up to next minute
    }

    /**
     * Record a failed login attempt
     * @return true if account is now locked
     */
    public boolean recordFailedAttempt(Long accountId) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + accountId;

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == null) {
            attempts = 1L;
        }

        if (attempts == 1) {
            // First attempt - set TTL for the counter
            redisTemplate.expire(attemptsKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }

        log.info("Failed login attempt {} for account {}", attempts, accountId);

        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(accountId);
            return true;
        }

        return false;
    }

    /**
     * Lock the account for LOCK_DURATION_MINUTES
     */
    private void lockAccount(Long accountId) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + accountId;
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + accountId;

        // Set lock flag with TTL
        redisTemplate.opsForValue().set(lockKey, "LOCKED", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        // Clear attempts counter
        redisTemplate.delete(attemptsKey);

        log.warn("Account {} has been locked for {} minutes due to {} failed login attempts",
                accountId, LOCK_DURATION_MINUTES, MAX_ATTEMPTS);
    }

    /**
     * Clear failed attempts on successful login
     */
    public void clearFailedAttempts(Long accountId) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + accountId;
        redisTemplate.delete(attemptsKey);
        log.debug("Cleared failed login attempts for account {}", accountId);
    }

    /**
     * Get current failed attempt count
     */
    public int getFailedAttemptCount(Long accountId) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + accountId;
        String count = redisTemplate.opsForValue().get(attemptsKey);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * Get remaining attempts before lock
     */
    public int getRemainingAttempts(Long accountId) {
        return MAX_ATTEMPTS - getFailedAttemptCount(accountId);
    }

    /**
     * Get max allowed attempts
     */
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    /**
     * Get lock duration in minutes
     */
    public long getLockDurationMinutes() {
        return LOCK_DURATION_MINUTES;
    }
}

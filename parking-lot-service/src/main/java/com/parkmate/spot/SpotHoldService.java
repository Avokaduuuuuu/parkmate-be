package com.parkmate.spot;

import com.parkmate.config.RedisPrefix;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotHoldService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SpotRepository spotRepository;


    public boolean holdSpot(Long userId, Long spotId) {
        String key = RedisPrefix.SPOT_HOLD + spotId;

        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(userId), RedisPrefix.HOLD_DURATION_SECONDS, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(success)) {
            log.info("Spot held successfully: spotId={}, userId={}", spotId, userId);
            return true;
        } else {
            log.info("Spot held failed: spotId={}, userId={}", spotId, userId);
            return false;
        }
    }

    public void releaseHold(Long userId, Long spotId) {
        String key = RedisPrefix.SPOT_HOLD + spotId;
        String heldByUserId = redisTemplate.opsForValue().get(key);

        if (heldByUserId != null && heldByUserId.equals(String.valueOf(userId))) {
            redisTemplate.delete(key);
            log.info("Spot hold released: spotId={}, userId={}", spotId, userId);
        } else {
            throw new AppException(ErrorCode.SPOT_RELEASED_FAILED, "\"You cannot release this spot hold\"");
        }
    }

    public Boolean isSpotHold(Long spotId) {
        String key = RedisPrefix.SPOT_HOLD + spotId;
        String heldByUserId = redisTemplate.opsForValue().get(key);
        log.info("Spot hold: spotId={}, userId={}", spotId, heldByUserId);
        return heldByUserId != null;
    }
}

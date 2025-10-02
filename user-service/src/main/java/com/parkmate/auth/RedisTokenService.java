package com.parkmate.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    String REFRESH_TOKEN_PREFIX = "refresh_token:";


    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void storeRefreshToken(String token, Map<String, Object> userInfo, long seconds) {
        try {
            String key = REFRESH_TOKEN_PREFIX + token;
            String json = objectMapper.writeValueAsString(userInfo);
            redisTemplate.opsForValue().set(key, json, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.STORE_REFRESH_TOKEN_FAILED, "Failed to store refresh token in Redis");
        }
    }

    public Map<String, Object> getUserInfo(String token) {
        try {
            String key = REFRESH_TOKEN_PREFIX + token;
            String json = redisTemplate.opsForValue().get(key);
            return json != null ? objectMapper.readValue(json, Map.class) : null;
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_INFO_NOT_FOUND, "Failed to retrieve user info from Redis");
        }
    }

    public void deleteRefreshToken(String token) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
    }
}
package com.parkmate.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void storeRefreshToken(String token, Map<String, Object> userInfo, long seconds) {
        try {
            String key = "refresh_token:" + token;
            String json = objectMapper.writeValueAsString(userInfo);
            redisTemplate.opsForValue().set(key, json, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store refresh token", e);
        }
    }

    public Map<String, Object> getUserInfo(String token) {
        try {
            String key = "refresh_token:" + token;
            String json = redisTemplate.opsForValue().get(key);
            return json != null ? objectMapper.readValue(json, Map.class) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteRefreshToken(String token) {
        redisTemplate.delete("refresh_token:" + token);
    }
}
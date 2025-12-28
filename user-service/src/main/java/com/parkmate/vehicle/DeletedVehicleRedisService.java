package com.parkmate.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.vehicle.dto.DeletedVehicleInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletedVehicleRedisService {

    private static final String VEHICLE_BLOCK_PREFIX = "vehicle:deleted:block:";
    private static final String VEHICLE_SYNC_PREFIX = "vehicle:deleted:sync:";

    private static final long BLOCK_TTL_HOURS = 2;
    private static final long SYNC_TTL_DAYS = 1;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Store deleted vehicle info in Redis with two TTLs:
     * - Block key (2 hours): Prevents user from creating vehicle with same license plate
     * - Sync key (1 day): For parking lot local database synchronization
     */
    public void storeDeletedVehicle(Vehicle vehicle) {
        try {
            DeletedVehicleInfo info = DeletedVehicleInfo.builder()
                    .vehicleId(vehicle.getId())
                    .userId(vehicle.getUser().getId())
                    .licensePlate(vehicle.getLicensePlate())
                    .vehicleType(vehicle.getVehicleType())
                    .vehicleBrand(vehicle.getVehicleBrand())
                    .vehicleModel(vehicle.getVehicleModel())
                    .vehicleColor(vehicle.getVehicleColor())
                    .isElectric(vehicle.isElectric())
                    .deletedAt(LocalDateTime.now())
                    .build();

            String json = objectMapper.writeValueAsString(info);

            // Store block key with 2 hours TTL
            String blockKey = VEHICLE_BLOCK_PREFIX + vehicle.getLicensePlate();
            redisTemplate.opsForValue().set(blockKey, json, BLOCK_TTL_HOURS, TimeUnit.HOURS);

            // Store sync key with 1 day TTL
            String syncKey = VEHICLE_SYNC_PREFIX + vehicle.getLicensePlate();
            redisTemplate.opsForValue().set(syncKey, json, SYNC_TTL_DAYS, TimeUnit.DAYS);

            log.info("Stored deleted vehicle info in Redis for license plate: {}", vehicle.getLicensePlate());
        } catch (Exception e) {
            log.error("Failed to store deleted vehicle in Redis: {}", e.getMessage());
        }
    }

    /**
     * Check if a license plate is blocked (within 2-hour cooldown period)
     */
    public boolean isLicensePlateBlocked(String licensePlate) {
        String blockKey = VEHICLE_BLOCK_PREFIX + licensePlate;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    /**
     * Get deleted vehicle info from block key
     */
    public DeletedVehicleInfo getBlockedVehicleInfo(String licensePlate) {
        try {
            String blockKey = VEHICLE_BLOCK_PREFIX + licensePlate;
            String json = redisTemplate.opsForValue().get(blockKey);
            if (json != null) {
                return objectMapper.readValue(json, DeletedVehicleInfo.class);
            }
        } catch (Exception e) {
            log.error("Failed to get blocked vehicle info from Redis: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get deleted vehicle info from sync key (for parking lot sync)
     */
    public DeletedVehicleInfo getSyncVehicleInfo(String licensePlate) {
        try {
            String syncKey = VEHICLE_SYNC_PREFIX + licensePlate;
            String json = redisTemplate.opsForValue().get(syncKey);
            if (json != null) {
                return objectMapper.readValue(json, DeletedVehicleInfo.class);
            }
        } catch (Exception e) {
            log.error("Failed to get sync vehicle info from Redis: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Remove sync key after parking lot has synced (optional)
     */
    public void removeSyncKey(String licensePlate) {
        String syncKey = VEHICLE_SYNC_PREFIX + licensePlate;
        redisTemplate.delete(syncKey);
        log.info("Removed sync key for license plate: {}", licensePlate);
    }

    /**
     * Get all deleted vehicles pending sync
     * Returns Map<licensePlate, userId>
     * Uses SCAN instead of KEYS to avoid blocking Redis
     */
    public java.util.Map<String, Long> getDeletedVehiclesForSync() {
        java.util.Map<String, Long> result = new java.util.HashMap<>();
        try {
            org.springframework.data.redis.core.ScanOptions scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(VEHICLE_SYNC_PREFIX + "*")
                    .count(100)
                    .build();

            try (org.springframework.data.redis.core.Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null) {
                        DeletedVehicleInfo info = objectMapper.readValue(json, DeletedVehicleInfo.class);
                        result.put(info.getLicensePlate(), info.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get deleted vehicles for sync: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Remove multiple sync keys after parking lot has synced
     */
    public void removeSyncKeys(java.util.List<String> licensePlates) {
        if (licensePlates == null || licensePlates.isEmpty()) {
            return;
        }
        java.util.List<String> keys = licensePlates.stream()
                .map(lp -> VEHICLE_SYNC_PREFIX + lp)
                .toList();
        redisTemplate.delete(keys);
        log.info("Removed {} sync keys", licensePlates.size());
    }
}

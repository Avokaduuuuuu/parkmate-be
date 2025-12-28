package com.parkmate.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.vehicle.dto.AddedVehicleInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddedVehicleRedisService {

    private static final String VEHICLE_SYNC_PREFIX = "vehicle:added:sync:";
    private static final long SYNC_TTL_DAYS = 1;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Store added vehicle info in Redis for parking lot sync
     * Sync key expires after 1 day
     */
    public void storeAddedVehicle(Vehicle vehicle) {
        try {
            AddedVehicleInfo info = AddedVehicleInfo.builder()
                    .vehicleId(vehicle.getId())
                    .userId(vehicle.getUser().getId())
                    .licensePlate(vehicle.getLicensePlate())
                    .vehicleType(vehicle.getVehicleType())
                    .vehicleBrand(vehicle.getVehicleBrand())
                    .vehicleModel(vehicle.getVehicleModel())
                    .vehicleColor(vehicle.getVehicleColor())
                    .isElectric(vehicle.isElectric())
                    .addedAt(LocalDateTime.now())
                    .build();

            String json = objectMapper.writeValueAsString(info);

            // Store sync key with 1 day TTL
            String syncKey = VEHICLE_SYNC_PREFIX + vehicle.getLicensePlate();
            redisTemplate.opsForValue().set(syncKey, json, SYNC_TTL_DAYS, TimeUnit.DAYS);

            log.info("Stored added vehicle info in Redis for license plate: {}", vehicle.getLicensePlate());
        } catch (Exception e) {
            log.error("Failed to store added vehicle in Redis: {}", e.getMessage());
        }
    }

    /**
     * Get added vehicle info from sync key
     */
    public AddedVehicleInfo getSyncVehicleInfo(String licensePlate) {
        try {
            String syncKey = VEHICLE_SYNC_PREFIX + licensePlate;
            String json = redisTemplate.opsForValue().get(syncKey);
            if (json != null) {
                return objectMapper.readValue(json, AddedVehicleInfo.class);
            }
        } catch (Exception e) {
            log.error("Failed to get sync vehicle info from Redis: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get all added vehicles pending sync
     * Returns List of SyncResponse containing userId, licensePlate, vehicleType
     * Uses SCAN instead of KEYS to avoid blocking Redis
     */
    public List<AddedVehicleInfo.SyncResponse> getAddedVehiclesForSync() {
        List<AddedVehicleInfo.SyncResponse> result = new java.util.ArrayList<>();
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
                        AddedVehicleInfo info = objectMapper.readValue(json, AddedVehicleInfo.class);
                        result.add(info.toSyncResponse());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get added vehicles for sync: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Remove sync key after parking lot has synced
     */
    public void removeSyncKey(String licensePlate) {
        String syncKey = VEHICLE_SYNC_PREFIX + licensePlate;
        redisTemplate.delete(syncKey);
        log.info("Removed added vehicle sync key for license plate: {}", licensePlate);
    }

    /**
     * Remove multiple sync keys after parking lot has synced
     */
    public void removeSyncKeys(List<String> licensePlates) {
        if (licensePlates == null || licensePlates.isEmpty()) {
            return;
        }
        List<String> keys = licensePlates.stream()
                .map(lp -> VEHICLE_SYNC_PREFIX + lp)
                .toList();
        redisTemplate.delete(keys);
        log.info("Removed {} added vehicle sync keys", licensePlates.size());
    }
}

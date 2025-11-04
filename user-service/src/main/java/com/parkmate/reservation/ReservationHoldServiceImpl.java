package com.parkmate.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.reservation.dto.HoldSlotRequest;
import com.parkmate.reservation.dto.HoldSlotResponse;
import com.parkmate.vehicle.Vehicle;
import com.parkmate.vehicle.VehicleRepository;
import com.parkmate.vehicle.VehicleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationHoldServiceImpl implements ReservationHoldService {

    private static final String HOLD_KEY_PREFIX = "reservation:hold:";
    private static final String PARKING_LOT_HOLDS_PREFIX = "reservation:holds:lot:";
    private static final long HOLD_TTL_MINUTES = 15;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final VehicleRepository vehicleRepository;

    @Override
    public HoldSlotResponse holdSlot(HoldSlotRequest request) {
        // Get vehicle to extract vehicleType
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        String holdId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_TTL_MINUTES);

        TemporaryReservationHold hold = TemporaryReservationHold.builder()
                .holdId(holdId)
                .parkingLotId(request.getParkingLotId())
                .userId(request.getUserId())
                .vehicleId(request.getVehicleId())
                .vehicleType(vehicle.getVehicleType())  // Get from DB
                .reservedFrom(request.getReservedFrom())
                .assumedStayMinute(request.getAssumedStayMinute())
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        try {
            String holdKey = HOLD_KEY_PREFIX + holdId;
            String holdJson = objectMapper.writeValueAsString(hold);
            redisTemplate.opsForValue().set(holdKey, holdJson, HOLD_TTL_MINUTES, TimeUnit.MINUTES);

            String lotHoldsKey = PARKING_LOT_HOLDS_PREFIX + request.getParkingLotId();
            redisTemplate.opsForSet().add(lotHoldsKey, holdId);
            redisTemplate.expire(lotHoldsKey, HOLD_TTL_MINUTES + 1, TimeUnit.MINUTES);

            log.info("Created temporary hold: {} for parking lot: {}, user: {}, expires: {}",
                    holdId, request.getParkingLotId(), request.getUserId(), expiresAt);

            return HoldSlotResponse.builder()
                    .holdId(holdId)
                    .parkingLotId(request.getParkingLotId())
                    .userId(request.getUserId())
                    .reservedFrom(request.getReservedFrom())
                    .assumedStayMinute(request.getAssumedStayMinute())
                    .expiresAt(expiresAt)
                    .message("Slot held successfully for 15 minutes")
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize hold data", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Failed to hold slot");
        }
    }

    @Override
    public void releaseHold(String holdId) {
        try {
            String holdKey = HOLD_KEY_PREFIX + holdId;
            String holdJson = redisTemplate.opsForValue().get(holdKey);

            if (holdJson == null) {
                log.warn("Hold not found or already expired: {}", holdId);
                return;
            }

            TemporaryReservationHold hold = objectMapper.readValue(holdJson, TemporaryReservationHold.class);

            // Remove from individual hold
            redisTemplate.delete(holdKey);

            // Remove from parking lot's set
            String lotHoldsKey = PARKING_LOT_HOLDS_PREFIX + hold.getParkingLotId();
            redisTemplate.opsForSet().remove(lotHoldsKey, holdId);

            log.info("Released hold: {} for parking lot: {}", holdId, hold.getParkingLotId());

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize hold data for holdId: {}", holdId, e);
        }
    }

    @Override
    public Long countTemporaryHolds(Long parkingLotId, String vehicleTypeStr, String reservedFromStr, Integer assumedStayMinute) {
        try {
            String lotHoldsKey = PARKING_LOT_HOLDS_PREFIX + parkingLotId;
            Set<String> holdIds = redisTemplate.opsForSet().members(lotHoldsKey);

            if (holdIds == null || holdIds.isEmpty()) {
                return 0L;
            }

            LocalDateTime requestedStart = LocalDateTime.parse(reservedFromStr);
            LocalDateTime requestedEnd = requestedStart.plusMinutes(assumedStayMinute);
            VehicleType requestedVehicleType = VehicleType.valueOf(vehicleTypeStr);

            long count = 0;

            for (String holdId : holdIds) {
                String holdKey = HOLD_KEY_PREFIX + holdId;
                String holdJson = redisTemplate.opsForValue().get(holdKey);

                if (holdJson == null) {
                    // Hold expired, remove from set
                    redisTemplate.opsForSet().remove(lotHoldsKey, holdId);
                    continue;
                }

                TemporaryReservationHold hold = objectMapper.readValue(holdJson, TemporaryReservationHold.class);

                // Check if vehicle type matches
                if (hold.getVehicleType() != requestedVehicleType) {
                    continue;
                }

                // Check time overlap
                LocalDateTime holdStart = hold.getReservedFrom();
                LocalDateTime holdEnd = holdStart.plusMinutes(hold.getAssumedStayMinute());

                boolean overlaps = !(requestedEnd.isBefore(holdStart) || requestedStart.isAfter(holdEnd) ||
                        requestedEnd.isEqual(holdStart) || requestedStart.isEqual(holdEnd));

                if (overlaps) {
                    count++;
                }
            }

            log.debug("Counted {} temporary holds for parking lot: {}, vehicleType: {}, time: {} to {}",
                    count, parkingLotId, vehicleTypeStr, requestedStart, requestedEnd);

            return count;

        } catch (Exception e) {
            log.error("Error counting temporary holds for parking lot: {}", parkingLotId, e);
            return 0L; // Fail gracefully
        }
    }

    @Override
    public boolean isHoldValid(String holdId) {
        String holdKey = HOLD_KEY_PREFIX + holdId;
        return redisTemplate.hasKey(holdKey);
    }

    @Override
    public TemporaryReservationHold getHold(String holdId) {
        try {
            String holdKey = HOLD_KEY_PREFIX + holdId;
            String holdJson = redisTemplate.opsForValue().get(holdKey);

            if (holdJson == null) {
                return null;
            }

            return objectMapper.readValue(holdJson, TemporaryReservationHold.class);

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize hold data for holdId: {}", holdId, e);
            return null;
        }
    }
}
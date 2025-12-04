package com.parkmate.spot;

import com.parkmate.area.AreaEntity;
import com.parkmate.area.AreaRepository;
import com.parkmate.area.enums.AreaType;
import com.parkmate.client.UserClient;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.spot.dto.req.SpotCreateRequest;
import com.parkmate.spot.dto.req.SpotUpdateRequest;
import com.parkmate.spot.dto.resp.SpotResponse;
import com.parkmate.spot.enums.SpotStatus;
import com.parkmate.spot.enums.SpotUnavailableReason;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {
    private final SpotRepository spotRepository;
    private final AreaRepository areaRepository;
    private final SpotHoldService spotHoldService;
    private final UserClient userClient;

    @Override
    public SpotResponse findById(Long id) {
        return SpotMapper.INSTANCE.toResponse(
                spotRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.SPOT_NOT_FOUND))
        );
    }

    @Override
    public List<SpotResponse> findAll(SpotFilterParams params) {
        return spotRepository.findAll(params.getSpecification())
                .stream().map(SpotMapper.INSTANCE::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SpotResponse> addSpots(List<SpotCreateRequest> requests, Long areaId) {
        AreaEntity area = areaRepository.findById(areaId).orElseThrow(
                () -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND)
        );

        if (area.getVehicleType().equals(VehicleType.BIKE) || area.getVehicleType().equals(VehicleType.MOTORBIKE)) {
            throw new AppException(ErrorCode.VEHICLE_TYPE_MISS_MATCH);
        }
        List<SpotEntity> spotEntities = toSpotEntities(requests, area);

        area.setTotalSpots(area.getTotalSpots() + spotEntities.size());
        areaRepository.save(area);
        return spotRepository.saveAll(spotEntities)
                .stream().map(SpotMapper.INSTANCE::toResponse).collect(Collectors.toList());
    }

    private List<SpotEntity> toSpotEntities(List<SpotCreateRequest> requests, AreaEntity area) {
        return requests.stream()
                .map(spot -> {
                    SpotEntity spotEntity = SpotMapper.INSTANCE.toEntity(spot);
                    spotEntity.setStatus(SpotStatus.AVAILABLE);
                    spotEntity.setParkingArea(area);
                    return spotEntity;
                })
                .toList();
    }

    @Override
    public SpotResponse updateSpot(Long id, SpotUpdateRequest request) {
        SpotEntity spotEntity = spotRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SPOT_NOT_FOUND)
        );
        if (request.status() != null) {
            spotEntity.setStatus(request.status());
            if (request.status().equals(SpotStatus.MAINTENANCE) || request.status().equals(SpotStatus.DISABLED)) {
                if (request.blockReason() == null) throw new AppException(ErrorCode.BLOCK_REASON_REQUIRED);
                spotEntity.setBlockReason(request.blockReason());
            }
        }
        if (request.name() != null) spotEntity.setName(request.name());

        return SpotMapper.INSTANCE.toResponse(spotRepository.save(spotEntity));
    }

    @Override
    public SpotResponse deleteSpot(Long id) {
        SpotEntity spotEntity = spotRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SPOT_NOT_FOUND)
        );
        spotEntity.setStatus(SpotStatus.DISABLED);
        return SpotMapper.INSTANCE.toResponse(spotRepository.save(spotEntity));
    }

    @Override
    public SpotResponse holdSpot(Long id, Long userId) {
        SpotEntity spotEntity = spotRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SPOT_NOT_FOUND)
        );
        if (!spotHoldService.holdSpot(userId, id)) throw new AppException(ErrorCode.SPOT_HELD_FAILED);
        SpotResponse spotResponse = SpotMapper.INSTANCE.toResponse(spotEntity);
        spotResponse.setHasSession(true);
        return spotResponse;
    }

    @Override
    public SpotResponse releaseSpot(Long id, Long userId) {
        SpotEntity spotEntity = spotRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.SPOT_NOT_FOUND)
        );
        spotHoldService.releaseHold(userId, id);
        SpotResponse spotResponse = SpotMapper.INSTANCE.toResponse(spotEntity);
        spotResponse.setHasSession(false);
        return spotResponse;
    }

    @Override
    public Long count() {
        return spotRepository.count();
    }


    @Override
    public List<SpotResponse> getSubscriptionAvailability(
            Long areaId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        AreaEntity area = areaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND));

        List<SpotEntity> spots = spotRepository.findByParkingArea_Id(areaId);

        List<Long> spotIds = spots.stream().map(SpotEntity::getId).toList();
        List<Long> occupiedSpotIds = userClient.checkOccupiedSpots(spotIds, startDateTime, endDateTime);
        return spots.stream().map(spot -> {
            SpotResponse response = SpotMapper.INSTANCE.toResponse(spot);

            String reason = null;
            boolean isAvailable = true;

            if (area.getAreaType() != AreaType.SUBSCRIPTION_ONLY) {
                isAvailable = false;
                reason = SpotUnavailableReason.NOT_SUBSCRIPTION_AREA.name();
            } else if (occupiedSpotIds.contains(spot.getId())) {
                isAvailable = false;
                reason = SpotUnavailableReason.ALREADY_ASSIGNED.name();
            } else if (spotHoldService.isSpotHold(spot.getId())) {
                isAvailable = false;
                reason = SpotUnavailableReason.SPOT_HELD.name();
            } else if (spot.getStatus() != SpotStatus.AVAILABLE) {
                isAvailable = false;
                reason = SpotUnavailableReason.ALREADY_ASSIGNED.name();
            }
            response.setIsAvailableForSubscription(isAvailable);
            response.setSubscriptionUnavailabilityReason(reason);
            return response;
        }).toList();
    }
}

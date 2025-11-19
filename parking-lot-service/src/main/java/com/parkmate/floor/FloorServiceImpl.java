package com.parkmate.floor;

import com.parkmate.area.AreaMapper;
import com.parkmate.area.AreaRepository;
import com.parkmate.area.dto.resp.AreaResponse;
import com.parkmate.client.UserClient;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.floor.dto.resp.FloorDetailedResponse;
import com.parkmate.floor.dto.resp.FloorResponse;
import com.parkmate.floor_capacity.FloorCapacityEntity;
import com.parkmate.floor_capacity.FloorCapacityMapper;
import com.parkmate.floor_capacity.dto.req.FloorCapacityCreateRequest;
import com.parkmate.floor_capacity.dto.resp.FloorCapacityResponse;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.spot.SpotHoldService;
import com.parkmate.spot.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FloorServiceImpl implements FloorService {
    private final FloorRepository floorRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final AreaRepository areaRepository;
    private final SpotRepository spotRepository;
    private final UserClient userClient;
    private final SpotHoldService spotHoldService;

    @Override
    public FloorResponse createFloor(Long parkingLotId, FloorCreateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        FloorEntity floorEntity = FloorEntity.builder()
                .floorName(request.floorName())
                .floorNumber(request.floorNumber())
                .floorTopLeftX(request.floorTopLeftX())
                .floorTopLeftY(request.floorTopLeftY())
                .floorWidth(request.floorWidth())
                .floorHeight(request.floorHeight())
                .parkingLot(parkingLotEntity)
                .build();

        floorEntity.setParkingFloorCapacity(toFloorCapacityEntities(request.capacityRequests(), floorEntity));

        return FloorMapper.INSTANCE.toResponse(floorRepository.save(floorEntity));
    }

    @Override
    public FloorDetailedResponse getFloorById(Long parkingLotId) {
        return FloorMapper.INSTANCE.toResponseDetailed(
                floorRepository.findById(parkingLotId)
                        .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND))
        );
    }

    @Override
    public Page<FloorResponse> findAll(int page, int size, String sortBy, String sortOrder, FloorFilterParams params) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FloorEntity> parkingFloorEntities = floorRepository.findAll(params.getSpecification(), pageable);
        return parkingFloorEntities.map(FloorMapper.INSTANCE::toResponse);
    }

    @Override
    public void deleteFloor(Long id) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (!floorEntity.getParkingLot().getStatus().equals(ParkingLotStatus.PREPARING) &&
                !floorEntity.getParkingLot().getStatus().equals(ParkingLotStatus.MAP_DENIED)
        ) {
            throw new AppException(ErrorCode.UNABLE_TO_DELETE_MAP, "Can not delete floor if not in PREPARING or MAP_DENIED phase");
        }

        floorRepository.delete(floorEntity);
    }

    @Override
    public FloorResponse updateFloor(Long id, FloorUpdateRequest request) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (request.floorName() != null) floorEntity.setFloorName(request.floorName());
        if (request.floorNumber() != null) floorEntity.setFloorNumber(request.floorNumber());
        if (request.isActive() != null) floorEntity.setIsActive(request.isActive());
        if (request.floorTopLeftX() != null) floorEntity.setFloorTopLeftX(request.floorTopLeftX());
        if (request.floorTopLeftY() != null) floorEntity.setFloorTopLeftY(request.floorTopLeftY());
        if (request.floorWidth() != null) floorEntity.setFloorWidth(request.floorWidth());
        if (request.floorHeight() != null) floorEntity.setFloorHeight(request.floorHeight());
        return FloorMapper.INSTANCE.toResponse(floorRepository.save(floorEntity));
    }

    @Override
    public FloorDetailedResponse getFloorByIdAndVehicleType(Long id, VehicleType vehicleType) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        FloorDetailedResponse floorDetailedResponse = FloorDetailedResponse.builder()
                .id(floorEntity.getId())
                .floorName(floorEntity.getFloorName())
                .floorNumber(floorEntity.getFloorNumber())
                .isActive(floorEntity.getIsActive())
                .build();

        List<FloorCapacityResponse> floorCapacityResponses = floorEntity.getParkingFloorCapacity()
                .stream()
                .filter(capacity -> capacity.getVehicleType() == vehicleType)
                .map(FloorCapacityMapper.INSTANCE::toResponse)
                .toList();

        floorDetailedResponse.setParkingFloorCapacity(floorCapacityResponses);
        List<AreaResponse> areaResponses = floorEntity.getAreas()
                .stream()
                .filter(area -> area.getVehicleType() == vehicleType)
                .map(AreaMapper.INSTANCE::toResponse)
                .toList();

        floorDetailedResponse.setAreas(areaResponses);

        return floorDetailedResponse;
    }

    @Override
    public List<FloorResponse> getSubscriptionAvailability(
            Long parkingLotId,
            VehicleType vehicleType,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<FloorEntity> floors = floorRepository.findByParkingLot_IdAndIsActive(parkingLotId, true);

        return floors.stream()
                .map(floor -> {
                    FloorResponse response = FloorMapper.INSTANCE.toResponse(floor);
                    System.out.println("Subscription availability for floor " + floor.getId());
                    Integer totalSubscriptionSpots = areaRepository.countTotalSubscriptionSpots(
                            floor.getId(),
                            vehicleType
                    );
                    System.out.println("Total subscription spots: " + totalSubscriptionSpots);
                    List<Long> spotsIds = spotRepository.findIdsByFloorAndVehicleTypeAndSubscriptionOnly(
                            floor.getId(),
                            vehicleType
                    );

                    int available = 0;
                    if (totalSubscriptionSpots != null && totalSubscriptionSpots > 0) {
                        if (spotsIds.isEmpty()) {
                            available = totalSubscriptionSpots;
                        } else {
                            // Check occupied spots in database
                            List<Long> occupiedSpotIds = userClient.checkOccupiedSpots(spotsIds, startDate, endDate);
                            System.out.println("Occupied spots: " + occupiedSpotIds.size());

                            // Check held spots in Redis
                            long heldSpotsCount = spotsIds.stream()
                                    .filter(spotHoldService::isSpotHold)
                                    .count();
                            System.out.println("Held spots: " + heldSpotsCount);

                            available = totalSubscriptionSpots - occupiedSpotIds.size() - (int) heldSpotsCount;
                        }
                    }
                    System.out.println("Available subscription spots: " + available);

                    response.setTotalSubscriptionSpots(totalSubscriptionSpots != null ? totalSubscriptionSpots : 0);
                    response.setAvailableSubscriptionSpots(Math.max(0, available));

                    return response;

                })
                .toList();
    }


    @Override
    public Long count() {
        return floorRepository.count();
    }


    private List<FloorCapacityEntity> toFloorCapacityEntities(List<FloorCapacityCreateRequest> requests, FloorEntity floorEntity) {
        return requests.stream()
                .map(request -> FloorCapacityEntity.builder()
                        .capacity(request.capacity())
                        .vehicleType(request.vehicleType())
                        .supportElectricVehicle(request.supportElectricVehicle())
                        .parkingFloor(floorEntity)
                        .build()
                )
                .toList();
    }
}

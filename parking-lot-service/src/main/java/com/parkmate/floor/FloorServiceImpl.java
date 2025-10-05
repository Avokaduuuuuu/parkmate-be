package com.parkmate.floor;

import com.parkmate.floor.dto.resp.FloorDetailedResponse;
import com.parkmate.floor_capacity.dto.req.FloorCapacityCreateRequest;
import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.floor.dto.resp.FloorResponse;

import com.parkmate.floor_capacity.FloorCapacityEntity;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FloorServiceImpl implements FloorService {
    private final FloorRepository floorRepository;
    private final ParkingLotRepository parkingLotRepository;

    @Override
    public FloorResponse createFloor(Long parkingLotId, FloorCreateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        FloorEntity floorEntity = FloorEntity.builder()
                .floorName(request.floorName())
                .floorNumber(request.floorNumber())
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
    public FloorResponse deleteFloor(Long id) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (!floorEntity.getIsActive()) throw new AppException(ErrorCode.INVALID_PARKING_FLOOR_STATUS_TRANSITION);
        floorEntity.setIsActive(false);
        return FloorMapper.INSTANCE.toResponse(floorRepository.save(floorEntity));
    }

    @Override
    public FloorResponse updateFloor(Long id, FloorUpdateRequest request) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (request.floorName() != null) floorEntity.setFloorName(request.floorName());
        if (request.floorNumber() != null) floorEntity.setFloorNumber(request.floorNumber());
        return FloorMapper.INSTANCE.toResponse(floorRepository.save(floorEntity));
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

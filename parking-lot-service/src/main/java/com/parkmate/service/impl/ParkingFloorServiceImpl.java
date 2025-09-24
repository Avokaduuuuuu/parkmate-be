package com.parkmate.service.impl;

import com.parkmate.dto.req.ParkingFloorCapacityCreateRequest;
import com.parkmate.dto.req.ParkingFloorCreateRequest;
import com.parkmate.dto.req.ParkingFloorUpdateRequest;
import com.parkmate.dto.resp.ParkingFloorResponse;
import com.parkmate.entity.ParkingFloorCapacityEntity;
import com.parkmate.entity.ParkingFloorEntity;
import com.parkmate.entity.ParkingLotEntity;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.mapper.ParkingFloorMapper;
import com.parkmate.repository.ParkingFloorRepository;
import com.parkmate.repository.ParkingLotRepository;
import com.parkmate.service.ParkingFloorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkingFloorServiceImpl implements ParkingFloorService {
    private final ParkingFloorRepository parkingFloorRepository;
    private final ParkingLotRepository parkingLotRepository;

    @Override
    public ParkingFloorResponse createFloor(Long parkingLotId, ParkingFloorCreateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        ParkingFloorEntity parkingFloorEntity = ParkingFloorEntity.builder()
                .floorName(request.floorName())
                .floorNumber(request.floorNumber())
                .parkingLot(parkingLotEntity)
                .build();

        parkingFloorEntity.setParkingFloorCapacity(toFloorCapacityEntities(request.capacityRequests(), parkingFloorEntity));

        return ParkingFloorMapper.INSTANCE.toResponse(parkingFloorRepository.save(parkingFloorEntity));
    }

    @Override
    public ParkingFloorResponse getFloorById(Long parkingLotId) {
        return ParkingFloorMapper.INSTANCE.toResponse(
                parkingFloorRepository.findById(parkingLotId)
                        .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND))
        );
    }

    @Override
    public Page<ParkingFloorResponse> findAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ParkingFloorEntity> parkingFloorEntities = parkingFloorRepository.findAll(pageable);
        return parkingFloorEntities.map(ParkingFloorMapper.INSTANCE::toResponse);
    }

    @Override
    public ParkingFloorResponse deleteFloor(Long id) {
        ParkingFloorEntity floorEntity = parkingFloorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (!floorEntity.getIsActive()) throw new AppException(ErrorCode.INVALID_PARKING_FLOOR_STATUS_TRANSITION);
        floorEntity.setIsActive(false);
        return ParkingFloorMapper.INSTANCE.toResponse(parkingFloorRepository.save(floorEntity));
    }

    @Override
    public ParkingFloorResponse updateFloor(Long id, ParkingFloorUpdateRequest request) {
        ParkingFloorEntity floorEntity = parkingFloorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (request.floorName() != null) floorEntity.setFloorName(request.floorName());
        if (request.floorNumber() != null) floorEntity.setFloorNumber(request.floorNumber());
        return ParkingFloorMapper.INSTANCE.toResponse(parkingFloorRepository.save(floorEntity));
    }


    private List<ParkingFloorCapacityEntity> toFloorCapacityEntities(List<ParkingFloorCapacityCreateRequest> requests, ParkingFloorEntity parkingFloorEntity) {
        return requests.stream()
                .map(request -> ParkingFloorCapacityEntity.builder()
                        .capacity(request.capacity())
                        .vehicleType(request.vehicleType())
                        .supportElectricVehicle(request.supportElectricVehicle())
                        .parkingFloor(parkingFloorEntity)
                        .build()
                )
                .toList();
    }
}

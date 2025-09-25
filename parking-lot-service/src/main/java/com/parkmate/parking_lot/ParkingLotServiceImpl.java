package com.parkmate.parking_lot;

import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.parking_lot.dto.resp.ParkingLotResponse;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    @Override
    public Page<ParkingLotResponse> fetchAllParkingLots(
            int page,
            int size,
            String sortBy,
            String sortOrder
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder),sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ParkingLotEntity> parkingLotEntities = parkingLotRepository.findAll(pageable);
        return parkingLotEntities.map(ParkingLotMapper.INSTANCE::toResponse);
    }

    @Override
    public ParkingLotResponse getParkingLotById(Long id) {
        return ParkingLotMapper.INSTANCE.toResponse(
                parkingLotRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND))
        );
    }

    @Override
    @Transactional
    public ParkingLotResponse addParkingLot(ParkingLotCreateRequest request) {
        ParkingLotEntity parkingLotEntity = ParkingLotMapper.INSTANCE.toEntity(request);
        parkingLotEntity.setStatus(ParkingLotStatus.PENDING);
        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }

    @Override
    @Transactional
    public ParkingLotResponse updateParkingLot(Long id, ParkingLotUpdateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        if (request.name() != null) parkingLotEntity.setName(request.name());
        if (request.city() != null) parkingLotEntity.setCity(request.city());
        if (request.streetAddress() != null) parkingLotEntity.setStreetAddress(request.streetAddress());
        if (request.ward() != null) parkingLotEntity.setWard(request.ward());
        if (request.operatingHoursEnd() != null) parkingLotEntity.setOperatingHoursEnd(request.operatingHoursEnd());
        if (request.operatingHoursStart() != null) parkingLotEntity.setOperatingHoursStart(request.operatingHoursStart());
        if (request.latitude() != null) parkingLotEntity.setLatitude(request.latitude());
        if (request.longitude() != null) parkingLotEntity.setLongitude(request.longitude());
        if (request.totalFloors() != null) parkingLotEntity.setTotalFloors(request.totalFloors());

        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }

    @Override
    public ParkingLotResponse deleteParkingLot(Long id) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        if (parkingLotEntity.getStatus().equals(ParkingLotStatus.PENDING)) {
            throw new AppException(ErrorCode.INVALID_PARKING_LOT_STATUS_TRANSITION);
        }

        parkingLotEntity.setStatus(ParkingLotStatus.INACTIVE);
        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }
}

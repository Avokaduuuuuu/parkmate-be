package com.parkmate.parking_lot;

import com.parkmate.lot_capacity.LotCapacityEntity;
import com.parkmate.lot_capacity.LotCapacityMapper;
import com.parkmate.lot_capacity.dto.req.LotCapacityCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.parking_lot.dto.resp.ParkingLotDetailedResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotResponse;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.enums.RuleScope;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


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
            String sortOrder,
            ParkingLotFilterParams params
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder),sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ParkingLotEntity> parkingLotEntities = parkingLotRepository.findAll(params.getSpecification(),pageable);
        return parkingLotEntities.map(ParkingLotMapper.INSTANCE::toResponse);
    }

    @Override
    public ParkingLotDetailedResponse getParkingLotById(Long id) {
        return ParkingLotMapper.INSTANCE.toDetailedResponse(
                parkingLotRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND))
        );
    }

    @Override
    @Transactional
    public ParkingLotResponse addParkingLot(Long partnerId,ParkingLotCreateRequest request) {
        ParkingLotEntity parkingLotEntity = ParkingLotMapper.INSTANCE.toEntity(request);
        parkingLotEntity.setIs24Hour(request.is24Hour());
        parkingLotEntity.setPartnerId(partnerId);
        parkingLotEntity.setStatus(ParkingLotStatus.PENDING);
        parkingLotEntity.setLotCapacity(toLotCapacity(request.lotCapacityRequests(), parkingLotEntity));
        parkingLotEntity.setPricingRules(toPricingRules(request.pricingRuleCreateRequests(), parkingLotEntity));
        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }

    private List<LotCapacityEntity> toLotCapacity(List<LotCapacityCreateRequest> requests, ParkingLotEntity parkingLotEntity) {
        return requests.stream()
                .map(req -> LotCapacityEntity.builder()
                        .capacity(req.capacity())
                        .parkingLot(parkingLotEntity)
                        .vehicleType(req.vehicleType())
                        .supportElectricVehicle(req.supportElectricVehicle())
                        .build()).toList();
    }

    private List<PricingRuleEntity> toPricingRules(List<PricingRuleCreateRequest> requests, ParkingLotEntity parkingLotEntity) {
        return requests.stream()
                .map(req -> PricingRuleEntity.builder()
                        .parkingLot(parkingLotEntity)
                        .ruleName(req.ruleName())
                        .baseRate(req.baseRate())
                        .depositFee(req.depositFee())
                        .freeMinute(req.freeMinute())
                        .gracePeriodMinute(req.gracePeriodMinute())
                        .initialDurationMinute(req.initialDurationMinute())
                        .vehicleType(req.vehicleType())
                        .initialCharge(req.initialCharge())
                        .ruleScope(RuleScope.LOT_WIDE)
                        .validFrom(req.validFrom())
                        .validUntil(req.validTo())
                        .build()
                )
                .toList();
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
        if (request.status() != null) {
            parkingLotEntity.setStatus(request.status());
            if ((request.status() == ParkingLotStatus.REJECTED || request.status() == ParkingLotStatus.MAP_DENIED) && request.reason() == null) {
                throw new AppException(ErrorCode.REASON_REQUIRED);
            }
        }
        if (request.is24Hour() != null) parkingLotEntity.setIs24Hour(request.is24Hour());


        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }

    @Override
    public ParkingLotResponse deleteParkingLot(Long id) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND, "Reason is required for REJECTED or MAP_DENIED parking lot"));

        if (parkingLotEntity.getStatus().equals(ParkingLotStatus.PENDING)) {
            throw new AppException(ErrorCode.INVALID_PARKING_LOT_STATUS_TRANSITION);
        }

        parkingLotEntity.setStatus(ParkingLotStatus.INACTIVE);
        return ParkingLotMapper.INSTANCE.toResponse(parkingLotRepository.save(parkingLotEntity));
    }
}

package com.parkmate.area;

import com.parkmate.area.dto.req.AreaCreateRequest;
import com.parkmate.area.dto.req.AreaUpdateRequest;
import com.parkmate.area.dto.resp.AreaDetailedResponse;
import com.parkmate.area.dto.resp.AreaResponse;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.floor.FloorEntity;
import com.parkmate.floor.FloorRepository;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.pricing_rule.PricingRuleMapper;
import com.parkmate.pricing_rule.PricingRuleRepository;
import com.parkmate.spot.SpotEntity;
import com.parkmate.spot.SpotHoldService;
import com.parkmate.spot.SpotMapper;
import com.parkmate.spot.SpotRepository;
import com.parkmate.spot.dto.req.SpotCreateRequest;
import com.parkmate.spot.enums.SpotStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {
    private final AreaRepository areaRepository;
    private final FloorRepository floorRepository;
    private final SpotHoldService spotHoldService;
    private final PricingRuleRepository pricingRuleRepository;
    /**
     *
     * @param page the page number of retrieve (zero-based-index)
     * @param size number of items per page
     * @param sortBy the field name to sort by (e.g., "id", "name"
     * @param sortOrder the sort direction, either "ASC" for ascending or "DESC" for descending
     * @param params the filter params containing optional filters
     * @return a {@link Page} of {@link AreaResponse}
     */
    @Override
    public Page<AreaResponse> findAllAreas(int page, int size, String sortBy, String sortOrder, AreaFilterParams params) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AreaEntity> areas = areaRepository.findAll(params.getSpecification(), pageable);
        return areas.map(AreaMapper.INSTANCE::toResponse);
    }

    @Override
    public AreaDetailedResponse findAreaById(Long id) {
        AreaEntity area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND));
        AreaDetailedResponse response = AreaMapper.INSTANCE.toDetailResponse(area);
        response.getSpots().forEach(spot -> {
            spot.setHasSession(spotHoldService.isSpotHold(spot.getId()));
        });
        if (area.getPricingRule() == null) {
            PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findByIsActiveAndVehicleType(true, area.getVehicleType())
                    .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));
            response.setPricingRule(PricingRuleMapper.INSTANCE.toResponse(pricingRuleEntity));
        }
        return response;
    }

    @Override
    public AreaResponse createArea(AreaCreateRequest request, Long floorId) {
        FloorEntity floorEntity = floorRepository.findById(floorId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_FLOOR_NOT_FOUND));

        if (!floorEntity.getIsActive()) {
            throw new AppException(ErrorCode.INACTIVE_FLOOR, "Floor with id " + floorId + " is not active");
        }

        if (!request.vehicleType().equals(VehicleType.BIKE) && !request.vehicleType().equals(VehicleType.MOTORBIKE)
            && request.totalSpots() != request.spotRequests().size()
        ) {
            throw new AppException(ErrorCode.SPOT_COUNT_MISS_MATCH);
        }

        AreaEntity area = AreaEntity.builder()
                .name(request.name())
                .vehicleType(request.vehicleType())
                .areaTopLeftX(request.areaTopLeftX())
                .areaTopLeftY(request.areaTopLeftY())
                .areaWidth(request.areaWidth())
                .areaHeight(request.areaHeight())
                .supportElectricVehicle(request.supportElectricVehicle())
                .totalSpots(request.totalSpots())
                .parkingFloor(floorEntity)
                .build();
        area.setSpots(toSpotEntities(request.spotRequests(), area));
        return AreaMapper.INSTANCE.toResponse(areaRepository.save(area));
    }

    @Override
    public AreaResponse updateArea(AreaUpdateRequest request, Long id) {
        AreaEntity area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND));
        if (request.name() != null) area.setName(request.name());
        if (request.vehicleType() != null) area.setVehicleType(request.vehicleType());
        if (request.areaTopLeftX() != null) area.setAreaTopLeftX(request.areaTopLeftX());
        if (request.areaTopLeftY() != null) area.setAreaTopLeftY(request.areaTopLeftY());
        if (request.areaWidth() != null) area.setAreaWidth(request.areaWidth());
        if (request.areaHeight() != null) area.setAreaHeight(request.areaHeight());
        if (request.supportElectricVehicle() != null) area.setSupportElectricVehicle(request.supportElectricVehicle());
        if (request.isActive() != null) area.setIsActive(request.isActive());

        return AreaMapper.INSTANCE.toResponse(areaRepository.save(area));
    }

    @Override
    public void deleteArea(Long id) {
        AreaEntity area = areaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND));
        if (area.getParkingFloor().getParkingLot().getStatus().equals(ParkingLotStatus.PREPARING))
            throw new AppException(ErrorCode.UNABLE_TO_DELETE_MAP, "Can not delete area map if not in PREPARING phase");

        areaRepository.delete(area);

    }

    @Override
    public Long count() {
        return areaRepository.countAllBy();
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
}

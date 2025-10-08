package com.parkmate.pricing_rule;

import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.req.PricingRuleUpdateRequest;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import com.parkmate.area.AreaEntity;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.pricing_rule.enums.RuleScope;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.area.AreaRepository;
import com.parkmate.parking_lot.ParkingLotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PricingRuleServiceImpl implements PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final AreaRepository areaRepository;

    @Override
    public Page<PricingRuleResponse> findAllPricingRules(int page, int size, String sortBy, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PricingRuleEntity> pricingRuleEntities = pricingRuleRepository.findAll(pageable);
        return pricingRuleEntities.map(PricingRuleMapper.INSTANCE::toResponse);
    }

    @Override
    public PricingRuleResponse findPricingRuleById(Long id) {
        return PricingRuleMapper.INSTANCE.toResponse(
                pricingRuleRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND))
        );
    }

    @Override
    public PricingRuleResponse createPricingRule(Long parkingLotId, PricingRuleCreateRequest request) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));
        PricingRuleEntity pricingRuleEntity = PricingRuleEntity.builder()
                .ruleName(request.ruleName())
                .vehicleType(request.vehicleType())
                .baseRate(request.baseRate())
                .depositFee(request.depositFee())
                .initialCharge(request.initialCharge())
                .initialDurationMinute(request.initialDurationMinute())
                .freeMinute(request.freeMinute())
                .gracePeriodMinute(request.gracePeriodMinute())
                .validFrom(request.validFrom())
                .validUntil(request.validTo())
                .parkingLot(parkingLotEntity)
                .build();

        if (request.areaId() != null) {
            AreaEntity parkingAreaEntity = areaRepository.findById(request.areaId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND));
            pricingRuleEntity.setRuleScope(RuleScope.AREA_SPECIFIC);
            pricingRuleEntity.setParkingArea(parkingAreaEntity);
        }else {
            pricingRuleEntity.setRuleScope(RuleScope.LOT_WIDE);
        }
        return PricingRuleMapper.INSTANCE.toResponse(pricingRuleRepository.save(pricingRuleEntity));
    }

    @Override
    public PricingRuleResponse updatePricingRule(Long id, PricingRuleUpdateRequest request) {
        PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));
        if (request.ruleName() != null) pricingRuleEntity.setRuleName(request.ruleName());
        if (request.vehicleType() != null) pricingRuleEntity.setVehicleType(request.vehicleType());
        if (request.baseRate() != null) pricingRuleEntity.setBaseRate(request.baseRate());
        if (request.depositFee() != null) pricingRuleEntity.setDepositFee(request.depositFee());
        if (request.initialCharge() != null) pricingRuleEntity.setInitialCharge(request.initialCharge());
        if (request.initialDurationMinute() != null) pricingRuleEntity.setInitialDurationMinute(request.initialDurationMinute());
        if (request.freeMinute() != null) pricingRuleEntity.setFreeMinute(request.freeMinute());
        if (request.validFrom() != null) pricingRuleEntity.setValidFrom(request.validFrom());
        if (request.validTo() != null) pricingRuleEntity.setValidFrom(request.validTo());
        if (request.areaId() != null) {
            AreaEntity parkingAreaEntity = areaRepository.findById(request.areaId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARKING_AREA_NOT_FOUND));
            pricingRuleEntity.setParkingArea(parkingAreaEntity);
            pricingRuleEntity.setRuleScope(RuleScope.AREA_SPECIFIC);
        } else {
            pricingRuleEntity.setParkingArea(null);
            pricingRuleEntity.setRuleScope(RuleScope.LOT_WIDE);
        }
        return PricingRuleMapper.INSTANCE.toResponse(pricingRuleRepository.save(pricingRuleEntity));
    }

    @Override
    public PricingRuleResponse deletePricingRule(Long id) {
        PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));
        pricingRuleEntity.setIsActive(false);
        return PricingRuleMapper.INSTANCE.toResponse(pricingRuleRepository.save(pricingRuleEntity));
    }

    @Override
    public Long count() {
        return pricingRuleRepository.count();
    }
}

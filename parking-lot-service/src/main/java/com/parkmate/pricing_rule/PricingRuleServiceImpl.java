package com.parkmate.pricing_rule;

import com.parkmate.area.AreaRepository;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.req.PricingRuleUpdateRequest;
import com.parkmate.pricing_rule.dto.req.SyncedPricingRulesUpdateRequest;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import com.parkmate.session.enums.SyncStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                .stepRate(request.stepRate())
                .initialCharge(request.initialCharge())
                .initialDurationMinute(request.initialDurationMinute())
                .stepMinute(request.stepMinute())
                .validFrom(request.validFrom())
                .validUntil(request.validTo())
                .parkingLot(parkingLotEntity)
                .isActive(false)
                .build();
        return PricingRuleMapper.INSTANCE.toResponse(pricingRuleRepository.save(pricingRuleEntity));
    }

    @Override
    public PricingRuleResponse updatePricingRule(Long id, PricingRuleUpdateRequest request) {
        PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));
        if (request.ruleName() != null) pricingRuleEntity.setRuleName(request.ruleName());
        if (request.vehicleType() != null) pricingRuleEntity.setVehicleType(request.vehicleType());
        if (request.stepRate() != null) pricingRuleEntity.setStepRate(request.stepRate());
        if (request.initialCharge() != null) pricingRuleEntity.setInitialCharge(request.initialCharge());
        if (request.initialDurationMinute() != null)
            pricingRuleEntity.setInitialDurationMinute(request.initialDurationMinute());
        if (request.stepMinute() != null) pricingRuleEntity.setStepMinute(request.stepMinute());
        if (request.validFrom() != null) pricingRuleEntity.setValidFrom(request.validFrom());
        if (request.validTo() != null) pricingRuleEntity.setValidFrom(request.validTo());
        if (request.isActive() != null) {
            if (request.isActive()) {
                log.info("Activating pricing rule {} for parkingLot {}", id, pricingRuleEntity.getParkingLot().getId());
                // Only deactivate pricing rules within the SAME parking lot, not all parking lots!
                Long parkingLotId = pricingRuleEntity.getParkingLot().getId();
                List<PricingRuleEntity> pricingRuleEntities = pricingRuleRepository
                        .findAllByParkingLot_IdAndVehicleTypeAndIsActive(parkingLotId, pricingRuleEntity.getVehicleType(), true);
                for (PricingRuleEntity pr : pricingRuleEntities) {
                    if (!pr.getId().equals(id)) { // Don't deactivate the one we're activating
                        pr.setIsActive(false);
                        pricingRuleRepository.save(pr);
                        log.info("Deactivated pricing rule {} in same parking lot", pr.getId());
                    }
                }
                pricingRuleEntity.setIsActive(true);
            } else {
                pricingRuleEntity.setIsActive(false);
            }
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

    @Override
    public List<PricingRuleResponse> findAllSyncPricingRules(Long lotId, SyncStatus status) {
        return pricingRuleRepository.findAllByParkingLotIdAndSyncStatus(lotId, status)
                .stream().map(PricingRuleMapper.INSTANCE::toResponse).collect(Collectors.toList());
    }

    @Override
    public Integer updateSyncedPricingRules(SyncedPricingRulesUpdateRequest request) {
        return pricingRuleRepository.updateSyncedPricingRules(request.ruleIds(), request.status());
    }

    @Override
    public PricingRuleResponse findByParkingLotIdAndVehicleType(Long id, VehicleType vehicleType) {
        PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findByParkingLot_IdAndIsActiveAndVehicleType(id, true, vehicleType)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));
        return PricingRuleMapper.INSTANCE.toResponse(pricingRuleEntity);
    }
}

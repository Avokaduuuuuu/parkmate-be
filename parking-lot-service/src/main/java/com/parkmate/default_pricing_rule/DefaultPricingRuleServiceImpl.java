package com.parkmate.default_pricing_rule;

import com.parkmate.default_pricing_rule.dto.req.DefaultPricingRuleCreateRequest;
import com.parkmate.default_pricing_rule.dto.resp.DefaultPricingRuleResponse;
import com.parkmate.default_pricing_rule.id.DefaultPricingRuleId;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotRepository;
import com.parkmate.pricing_rule.PricingRuleEntity;
import com.parkmate.pricing_rule.PricingRuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultPricingRuleServiceImpl implements DefaultPricingRuleService {

    private final DefaultPricingRuleRepository defaultPricingRuleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final PricingRuleRepository pricingRuleRepository;

    @Override
    public DefaultPricingRuleResponse addNewDefaultPricingRule(DefaultPricingRuleCreateRequest request) {
        DefaultPricingRuleEntity defaultPricingRuleEntity = new DefaultPricingRuleEntity();

        parkingLotRepository.findById(request.lotId())
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND));

        PricingRuleEntity pricingRuleEntity = pricingRuleRepository.findById(request.pricingRuleId())
                .orElseThrow(() -> new AppException(ErrorCode.PRICING_RULE_NOT_FOUND));

        if (defaultPricingRuleRepository.existsByIdParkingLotIdAndIdVehicleType(request.lotId(), request.vehicleType())) {
            throw new AppException(ErrorCode.DUPLICATE_PRICING_RULE);
        }

        if (!pricingRuleEntity.getParkingLot().getId().equals(request.lotId())) {
            throw new AppException(ErrorCode.PRICING_RULE_LOT_MISMATCH, String.format("Pricing rule belongs to lot %d but trying to apply to lot %d",
                    pricingRuleEntity.getParkingLot().getId(), request.lotId()));
        }

        if (!pricingRuleEntity.getVehicleType().equals(request.vehicleType())) {
            throw new AppException(ErrorCode.VEHICLE_TYPE_MISS_MATCH, String.format("Expected vehicle type %s but pricing rule is for %s",
                    pricingRuleEntity.getVehicleType(), request.vehicleType()));
        }

        defaultPricingRuleEntity.setId(new DefaultPricingRuleId(request.lotId(), request.vehicleType()));
        defaultPricingRuleEntity.setPricingRule(pricingRuleEntity);
        return DefaultPricingRuleMapper.INSTANCE.toResponse(defaultPricingRuleRepository.save(defaultPricingRuleEntity));
    }
}

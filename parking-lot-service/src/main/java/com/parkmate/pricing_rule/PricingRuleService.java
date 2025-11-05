package com.parkmate.pricing_rule;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.req.PricingRuleUpdateRequest;
import com.parkmate.pricing_rule.dto.req.SyncedPricingRulesUpdateRequest;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import com.parkmate.session.enums.SyncStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PricingRuleService {
    Page<PricingRuleResponse> findAllPricingRules(
            int page, int size, String sortBy, String sortOrder
    );

    PricingRuleResponse findPricingRuleById(Long id);

    PricingRuleResponse createPricingRule(Long parkingLotId, PricingRuleCreateRequest request);

    PricingRuleResponse updatePricingRule(Long id, PricingRuleUpdateRequest request);

    PricingRuleResponse deletePricingRule(Long id);

    Long count();

    List<PricingRuleResponse> findAllSyncPricingRules(Long lotId, SyncStatus status);

    Integer updateSyncedPricingRules(SyncedPricingRulesUpdateRequest request);

    PricingRuleResponse findByParkingLotIdAndVehicleType(Long id, VehicleType vehicleType);
}

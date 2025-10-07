package com.parkmate.pricing_rule;

import com.parkmate.pricing_rule.dto.req.PricingRuleCreateRequest;
import com.parkmate.pricing_rule.dto.req.PricingRuleUpdateRequest;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PricingRuleService {
    Page<PricingRuleResponse> findAllPricingRules(
            int page, int size, String sortBy, String sortOrder
    );
    PricingRuleResponse findPricingRuleById(Long id);
    PricingRuleResponse createPricingRule(Long parkingLotId, PricingRuleCreateRequest request);
    PricingRuleResponse updatePricingRule(Long id, PricingRuleUpdateRequest request);
    PricingRuleResponse deletePricingRule(Long id);
}

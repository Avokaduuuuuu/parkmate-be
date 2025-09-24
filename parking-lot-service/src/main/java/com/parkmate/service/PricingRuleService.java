package com.parkmate.service;

import com.parkmate.dto.req.PricingRuleCreateRequest;
import com.parkmate.dto.req.PricingRuleUpdateRequest;
import com.parkmate.dto.resp.PricingRuleResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PricingRuleService {
    Page<PricingRuleResponse> findAllPricingRules(
            int page, int size, String sortBy, String sortOrder
    );
    PricingRuleResponse findPricingRuleById(UUID id);
    PricingRuleResponse createPricingRule(Long parkingLotId, PricingRuleCreateRequest request);
    PricingRuleResponse updatePricingRule(UUID id, PricingRuleUpdateRequest request);
    PricingRuleResponse deletePricingRule(UUID id);
}

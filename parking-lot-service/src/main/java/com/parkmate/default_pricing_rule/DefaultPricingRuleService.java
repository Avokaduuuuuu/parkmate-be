package com.parkmate.default_pricing_rule;

import com.parkmate.default_pricing_rule.dto.req.DefaultPricingRuleCreateRequest;
import com.parkmate.default_pricing_rule.dto.resp.DefaultPricingRuleResponse;

public interface DefaultPricingRuleService {

    DefaultPricingRuleResponse addNewDefaultPricingRule(DefaultPricingRuleCreateRequest request);
}

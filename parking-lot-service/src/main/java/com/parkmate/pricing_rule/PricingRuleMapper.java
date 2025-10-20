package com.parkmate.pricing_rule;

import com.parkmate.override_pricing_rule.OverridePricingRuleMapper;
import com.parkmate.pricing_rule.dto.resp.PricingRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {OverridePricingRuleMapper.class}
)
public interface PricingRuleMapper {
    PricingRuleMapper INSTANCE = Mappers.getMapper(PricingRuleMapper.class);

    PricingRuleResponse toResponse(PricingRuleEntity entity);
}

package com.parkmate.override_pricing_rule;

import com.parkmate.override_pricing_rule.dto.resp.OverridePricingRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface OverridePricingRuleMapper {
    OverridePricingRuleMapper INSTANCE = Mappers.getMapper(OverridePricingRuleMapper.class);

    OverridePricingRuleResponse toResponse(OverridePricingRuleEntity entity);
}

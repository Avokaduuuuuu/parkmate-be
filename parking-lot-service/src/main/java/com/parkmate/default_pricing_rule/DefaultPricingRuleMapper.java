package com.parkmate.default_pricing_rule;

import com.parkmate.default_pricing_rule.dto.resp.DefaultPricingRuleResponse;
import com.parkmate.pricing_rule.PricingRuleMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PricingRuleMapper.class}
)
public interface DefaultPricingRuleMapper {
    DefaultPricingRuleMapper INSTANCE = Mappers.getMapper(DefaultPricingRuleMapper.class);

    @Mapping(target = "lotId", source = "parkingLot.id")
    @Mapping(target = "lotName", source = "parkingLot.name")
    DefaultPricingRuleResponse toResponse(DefaultPricingRuleEntity entity);

}

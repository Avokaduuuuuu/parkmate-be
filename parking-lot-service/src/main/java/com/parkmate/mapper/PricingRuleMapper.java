package com.parkmate.mapper;

import com.parkmate.dto.resp.PricingRuleResponse;
import com.parkmate.entity.PricingRuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PricingRuleMapper {
    PricingRuleMapper INSTANCE = Mappers.getMapper(PricingRuleMapper.class);

    PricingRuleResponse toResponse(PricingRuleEntity entity);
}

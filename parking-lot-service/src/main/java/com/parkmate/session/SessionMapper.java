package com.parkmate.session;

import com.parkmate.pricing_rule.PricingRuleMapper;
import com.parkmate.session.dto.resp.SessionDetailedResponse;
import com.parkmate.session.dto.resp.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PricingRuleMapper.class}
)
public interface SessionMapper {
    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);

    @Mapping(target = "pricingRuleId", source = "pricingRule.id")
    @Mapping(target = "lotId", source = "parkingLot.id")
    SessionResponse toResponse(SessionEntity entity);

    @Mapping(target = "lotName", source = "parkingLot.name")
    SessionDetailedResponse toDetailedResponse(SessionEntity entity);
}

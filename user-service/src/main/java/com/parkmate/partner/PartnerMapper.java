package com.parkmate.partner;

import com.parkmate.common.config.MapStructConfig;
import com.parkmate.partner.dto.CreatePartnerRequest;
import com.parkmate.partner.dto.PartnerResponse;
import com.parkmate.partner.dto.UpdatePartnerRequest;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface PartnerMapper {

    @Mapping(target = "accounts", source = "accounts")
    PartnerResponse toDto(Partner partner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.parkmate.partner.PartnerStatus.APPROVED)")
    @Mapping(target = "suspensionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "partnerRegistration", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    Partner toEntity(CreatePartnerRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "partnerRegistration", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    void updateEntityFromDto(UpdatePartnerRequest dto, @MappingTarget Partner entity);
}

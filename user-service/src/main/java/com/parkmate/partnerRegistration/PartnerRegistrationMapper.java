package com.parkmate.partnerRegistration;

import com.parkmate.common.config.MapStructConfig;
import com.parkmate.partnerRegistration.dto.CreatePartnerRegistrationRequest;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationResponse;
import com.parkmate.partnerRegistration.dto.UpdatePartnerRegistrationRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PartnerRegistrationMapper {
    @Mapping(target = "partnerId", source = "partner.id")
    PartnerRegistrationResponse toDto(PartnerRegistration partnerRegistration);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "approvalNotes", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "partner", ignore = true)
    PartnerRegistration toEntity(CreatePartnerRegistrationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "taxNumber", ignore = true)
    @Mapping(target = "businessLicenseNumber", ignore = true)
    @Mapping(target = "businessLicenseFileUrl", ignore = true)
    @Mapping(target = "companyAddress", ignore = true)
    @Mapping(target = "companyPhone", ignore = true)
    @Mapping(target = "companyEmail", ignore = true)
    @Mapping(target = "businessDescription", ignore = true)
    @Mapping(target = "contactPersonName", ignore = true)
    @Mapping(target = "contactPersonPhone", ignore = true)
    @Mapping(target = "contactPersonEmail", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "partner", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdatePartnerRegistrationRequest dto, @MappingTarget PartnerRegistration entity);


}

package com.parkmate.partnerWithdrawalPeriod;

import com.parkmate.partnerWithdrawalPeriod.dto.response.PartnerWithdrawalPeriodResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartnerWithdrawalPeriodMapper {

    PartnerWithdrawalPeriodResponse toResponse(PartnerWithdrawalPeriod entity);
}
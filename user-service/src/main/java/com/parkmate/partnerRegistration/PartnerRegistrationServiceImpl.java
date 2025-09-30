package com.parkmate.partnerRegistration;

import com.parkmate.common.enums.RequestStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.partnerRegistration.dto.CreatePartnerRegistrationRequest;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationResponse;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationSearchRequest;
import com.parkmate.partnerRegistration.dto.UpdatePartnerRegistrationRequest;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PartnerRegistrationServiceImpl implements PartnerRegistrationService {

    private final PartnerRegistrationRepository partnerRegistrationRepository;
    private final PartnerRegistrationMapper mapper;

    @Override
    public PartnerRegistrationResponse registerPartner(CreatePartnerRegistrationRequest request) {
        validateDuplicateConstraint(request);
        PartnerRegistration partnerRegistration = mapper.toEntity(request);

        partnerRegistration.setStatus(RequestStatus.PENDING);

        PartnerRegistration savedEntity = partnerRegistrationRepository.save(partnerRegistration);
        return mapper.toDto(savedEntity);
    }

    @Override
    public PartnerRegistrationResponse getPartnerRegistrationById(Long id) {
        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));
        return mapper.toDto(partnerRegistration);
    }

    @Override
    public PartnerRegistrationResponse updatePartnerRegistration(Long id, UpdatePartnerRegistrationRequest request) {
        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));
        mapper.updateEntityFromDto(request, partnerRegistration);
        PartnerRegistration savedPartnerRegistration = partnerRegistrationRepository.save(partnerRegistration);
        return mapper.toDto(savedPartnerRegistration);
    }

    @Override
    public Page<PartnerRegistrationResponse> getPartnerRegistrations(PartnerRegistrationSearchRequest request, Pageable pageable) {
        Predicate predicate = PartnerRegistrationSpecification.buildPredicate(request.toCriteria());
        Page<PartnerRegistration> page = partnerRegistrationRepository.findAll(predicate, pageable);
        return page.map(mapper::toDto);
    }

    @Override
    public void deletePartnerRegistration(Long id) {
        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));
        partnerRegistration.setStatus(RequestStatus.REJECTED);
    }

    private void validateDuplicateConstraint(CreatePartnerRegistrationRequest request) {
        if (partnerRegistrationRepository.existsByTaxNumber(request.getTaxNumber())) {
            throw new AppException(ErrorCode.TAX_NUMBER_ALREADY_EXISTS);
        }
    }
}

package com.parkmate.service.impl;

import com.parkmate.dto.criteria.PartnerSearchCriteria;
import com.parkmate.dto.request.CreatePartnerRequest;
import com.parkmate.dto.request.UpdatePartnerRequest;
import com.parkmate.dto.response.PartnerResponse;
import com.parkmate.entity.Partner;
import com.parkmate.entity.enums.PartnerStatus;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.mapper.PartnerMapper;
import com.parkmate.repository.PartnerRepository;
import com.parkmate.service.PartnerService;
import com.parkmate.specification.PartnerSpecification;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PartnerServiceImpl implements PartnerService {


    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;


    @Override
    public Page<PartnerResponse> search(PartnerSearchCriteria criteria, Pageable pageable) {
        Predicate predicate = PartnerSpecification.buildPredicate(criteria);
        return partnerRepository.findAll(predicate, pageable).map(partnerMapper::toDto);
    }

    @Override
    public List<PartnerResponse> search(PartnerSearchCriteria criteria) {
        Predicate predicate = PartnerSpecification.buildPredicate(criteria);
        Iterable<Partner> partners = partnerRepository.findAll(predicate);
        List<Partner> partnerList = new ArrayList<>();
        partners.forEach(partnerList::add);
        return partnerList.stream().map(partnerMapper::toDto).toList();
    }

    @Override
    public PartnerResponse create(CreatePartnerRequest request) {

        Partner partner = partnerMapper.toEntity(request);
        partner = partnerRepository.save(partner);
        return partnerMapper.toDto(partner);

    }

    @Override
    public PartnerResponse update(long id, UpdatePartnerRequest request) {

        Partner partner = partnerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND, "Partner not found"));
        partnerMapper.updateEntityFromDto(request, partner);
        partner = partnerRepository.save(partner);
        return partnerMapper.toDto(partner);
    }

    @Override
    public PartnerResponse findById(long id) {
        Partner partner = partnerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND, "Partner not found"));
        return partnerMapper.toDto(partner);
    }

    @Override
    public void delete(long id) {
        Partner partner = partnerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND, "Partner not found"));
        partner.setStatus(PartnerStatus.DELETED);
        partnerRepository.delete(partner);
    }
}

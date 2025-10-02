package com.parkmate.partner;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.partner.dto.CreatePartnerRequest;
import com.parkmate.partner.dto.PartnerResponse;
import com.parkmate.partner.dto.PartnerSearchCriteria;
import com.parkmate.partner.dto.UpdatePartnerRequest;
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
    public Page<PartnerResponse> search(PartnerSearchCriteria criteria, int page, int size, String sortBy, String sortOrder) {
        Predicate predicate = PartnerSpecification.buildPredicate(criteria);
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
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

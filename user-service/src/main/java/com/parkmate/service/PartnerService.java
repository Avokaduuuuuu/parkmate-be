package com.parkmate.service;

import com.parkmate.dto.criteria.PartnerSearchCriteria;
import com.parkmate.dto.request.CreatePartnerRequest;
import com.parkmate.dto.request.UpdatePartnerRequest;
import com.parkmate.dto.response.PartnerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PartnerService {

    Page<PartnerResponse> search(PartnerSearchCriteria criteria, Pageable pageable);

    List<PartnerResponse> search(PartnerSearchCriteria criteria);

    PartnerResponse create(CreatePartnerRequest request);

    PartnerResponse update(long id, UpdatePartnerRequest request);

    PartnerResponse findById(long id);

    void delete(long id);

}

package com.parkmate.partner;

import com.parkmate.partner.dto.PartnerSearchCriteria;
import com.parkmate.partner.dto.CreatePartnerRequest;
import com.parkmate.partner.dto.UpdatePartnerRequest;
import com.parkmate.partner.dto.PartnerResponse;
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

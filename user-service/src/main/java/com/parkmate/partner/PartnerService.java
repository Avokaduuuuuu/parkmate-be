package com.parkmate.partner;

import com.parkmate.partner.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface PartnerService {

    Page<PartnerResponse> search(PartnerSearchCriteria criteria, int page, int size, String sortBy, String sortOrder);

    List<PartnerResponse> search(PartnerSearchCriteria criteria);

    PartnerResponse create(CreatePartnerRequest request);

    PartnerResponse update(long id, UpdatePartnerRequest request);

    PartnerResponse findById(long id);

    void delete(long id);

    @Transactional
    ImportPartnerResponse importPartnersFromExcel(MultipartFile file);

    long count();

    void exportPartnersToExcel(PartnerSearchCriteria criteria, java.io.OutputStream outputStream) throws java.io.IOException;
}

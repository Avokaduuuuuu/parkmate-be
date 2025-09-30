package com.parkmate.partnerRegistration;

import com.parkmate.partnerRegistration.dto.CreatePartnerRegistrationRequest;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationResponse;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationSearchRequest;
import com.parkmate.partnerRegistration.dto.UpdatePartnerRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartnerRegistrationService {

    PartnerRegistrationResponse registerPartner(CreatePartnerRegistrationRequest request);

    PartnerRegistrationResponse getPartnerRegistrationById(Long id);

    PartnerRegistrationResponse updatePartnerRegistration(Long id, UpdatePartnerRegistrationRequest request);

    Page<PartnerRegistrationResponse> getPartnerRegistrations(PartnerRegistrationSearchRequest request, Pageable pageable);

    void deletePartnerRegistration(Long id);


}

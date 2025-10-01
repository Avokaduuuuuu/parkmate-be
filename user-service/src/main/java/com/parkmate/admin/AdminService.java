package com.parkmate.admin;

import com.parkmate.admin.dto.PartnerRegistrationApproveRequest;
import com.parkmate.admin.dto.PartnerRegistrationRejectRequest;
import org.springframework.stereotype.Service;

@Service
public interface AdminService {

    void approvePartnerRegistration(Long reviewerId, Long partnerRegistrationId, PartnerRegistrationApproveRequest request);

    void rejectPartnerRegistration(Long reviewerId, Long partnerRegistrationId, PartnerRegistrationRejectRequest request);


}

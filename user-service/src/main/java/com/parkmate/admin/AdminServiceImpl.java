package com.parkmate.admin;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.admin.dto.PartnerRegistrationApproveRequest;
import com.parkmate.admin.dto.PartnerRegistrationRejectRequest;
import com.parkmate.common.enums.RequestStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.partner.Partner;
import com.parkmate.partner.PartnerRepository;
import com.parkmate.partner.PartnerStatus;
import com.parkmate.partnerRegistration.PartnerRegistration;
import com.parkmate.partnerRegistration.PartnerRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {


    private final PartnerRegistrationRepository partnerRegistrationRepository;
    private final AccountRepository accountRepository;
    private final PartnerRepository partnerRepository;

    @Override
    public void approvePartnerRegistration(Long reviewerId, Long partnerRegistrationId, PartnerRegistrationApproveRequest request) {

        Account reviewerAccount = accountRepository.findById(reviewerId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(partnerRegistrationId)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));

        partnerRegistration.setStatus(RequestStatus.APPROVED);
        partnerRegistration.setReviewer(reviewerAccount);
        partnerRegistration.setApprovalNotes(request.getApprovalNotes());
        PartnerRegistration updatedPartnerRegistration = partnerRegistrationRepository.save(partnerRegistration);

        Partner partner = createPartner(updatedPartnerRegistration);

        partnerRepository.save(partner);


    }

    @Override
    public void rejectPartnerRegistration(Long reviewerId, Long partnerRegistrationId, PartnerRegistrationRejectRequest request) {
        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(partnerRegistrationId)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));

        Account reviewerAccount = accountRepository.findById(reviewerId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        partnerRegistration.setStatus(RequestStatus.REJECTED);
        partnerRegistration.setReviewer(reviewerAccount);
        partnerRegistration.setRejectionReason(request.getRejectionReason());
        partnerRegistrationRepository.save(partnerRegistration);

    }

    private Partner createPartner(PartnerRegistration updatedPartnerRegistration) {
        return Partner.builder()
                .partnerRegistration(updatedPartnerRegistration)
                .businessLicenseFileUrl(updatedPartnerRegistration.getBusinessLicenseFileUrl())
                .businessDescription(updatedPartnerRegistration.getBusinessDescription())
                .companyAddress(updatedPartnerRegistration.getCompanyAddress())
                .companyName(updatedPartnerRegistration.getCompanyName())
                .companyEmail(updatedPartnerRegistration.getCompanyEmail())
                .businessLicenseNumber(updatedPartnerRegistration.getBusinessLicenseNumber())
                .status(PartnerStatus.APPROVED)
                .taxNumber(updatedPartnerRegistration.getTaxNumber())
                .companyPhone(updatedPartnerRegistration.getCompanyPhone())
                .build();
    }

}

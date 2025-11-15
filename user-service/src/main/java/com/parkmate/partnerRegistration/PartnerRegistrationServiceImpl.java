package com.parkmate.partnerRegistration;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.account.publisher.AccountEventPublisher;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.dto.request.CreateWalletRequest;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.common.enums.RequestStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.partner.Partner;
import com.parkmate.partner.PartnerRepository;
import com.parkmate.partner.PartnerStatus;
import com.parkmate.partnerRegistration.dto.CreatePartnerRegistrationRequest;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationResponse;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationSearchRequest;
import com.parkmate.partnerRegistration.dto.UpdatePartnerRegistrationRequest;
import com.parkmate.s3.S3Service;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
@Service
@Slf4j
public class PartnerRegistrationServiceImpl implements PartnerRegistrationService {

    private final PartnerRegistrationRepository partnerRegistrationRepository;
    private final AccountRepository accountRepository;
    private final PartnerRegistrationMapper mapper;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountEventPublisher accountEventPublisher;
    private final S3Service s3Service;
    private final PaymentClient paymentClient;

    @Override
    public PartnerRegistrationResponse registerPartner(CreatePartnerRegistrationRequest request) {
        validateDuplicateConstraint(request);

        String verificationToken = generateRandomToken();

        Account account = Account.builder()
                .email(request.getContactPersonEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AccountRole.PARTNER_OWNER)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerificationToken(verificationToken)
                .phoneVerified(false)
                .emailVerified(false)
                .build();

        Account savedAccount = accountRepository.save(account);

        PartnerRegistration partnerRegistration = mapper.toEntity(request);
        partnerRegistration.setStatus(RequestStatus.PENDING);
        partnerRegistration.setSubmittedAt(LocalDateTime.now());
        partnerRegistration.setSubmittedByAccount(savedAccount);

        PartnerRegistration savedEntity = partnerRegistrationRepository.save(partnerRegistration);

        try {
            accountEventPublisher.publishPartnerVerificationEvent(
                    savedAccount.getEmail(),
                    partnerRegistration.getCompanyName(),
                    verificationToken
            );
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", savedAccount.getEmail(), e);
        }
        return mapper.toDto(savedEntity);
    }


    @Override
    public PartnerRegistrationResponse getPartnerRegistrationById(Long id) {
        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));
        return convertToResponseWithPresignedUrl(partnerRegistration);
    }

    @Override
    public PartnerRegistrationResponse updatePartnerRegistration(Long id, UpdatePartnerRegistrationRequest request) {
        if (!request.isValid()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));

        if (request.getReviewerId() != null || request.getStatus() != null) {
            Long reviewerId = request.getReviewerId();
            if (reviewerId != null && !accountRepository.existsById(reviewerId)) {
                throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
            }
        }

        mapper.updateEntityFromDto(request, partnerRegistration);

        if (request.getStatus() != null) {
            switch (request.getStatus()) {
                case APPROVED -> approvePartnerRegistration(partnerRegistration, request);
                case REJECTED -> rejectPartnerRegistration(partnerRegistration, request);
                case PENDING -> {
                    // Allow setting status back to PENDING for re-review
                    partnerRegistration.setStatus(RequestStatus.PENDING);
                    partnerRegistration.setReviewedBy(null);
                    partnerRegistration.setReviewedAt(null);
                    partnerRegistration.setApprovalNotes(null);
                    partnerRegistration.setRejectionReason(null);
                }
            }
        }

        PartnerRegistration savedPartnerRegistration = partnerRegistrationRepository.save(partnerRegistration);
        return mapper.toDto(savedPartnerRegistration);
    }

    @Override
    public Page<PartnerRegistrationResponse> getPartnerRegistrations(PartnerRegistrationSearchRequest request, Pageable pageable) {
        Predicate predicate = PartnerRegistrationSpecification.buildPredicate(request.toCriteria());
        Page<PartnerRegistration> page = partnerRegistrationRepository.findAll(predicate, pageable);
        return page.map(this::convertToResponseWithPresignedUrl);
    }

    @Override
    public Page<PartnerRegistrationResponse> getPartnerRegistrations(PartnerRegistrationSearchRequest request, int page, int size, String sortBy, String sortOrder) {
        Predicate predicate = PartnerRegistrationSpecification.buildPredicate(request.toCriteria());
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        Page<PartnerRegistration> registrationPage = partnerRegistrationRepository.findAll(predicate, pageable);
        return registrationPage.map(this::convertToResponseWithPresignedUrl);
    }

    @Override
    public void deletePartnerRegistration(Long id) {
        PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PARTNER_REGISTRATION_NOT_FOUND));
        partnerRegistration.setStatus(RequestStatus.REJECTED);
        partnerRegistrationRepository.save(partnerRegistration);
    }

    private void validateDuplicateConstraint(CreatePartnerRegistrationRequest request) {
        if (partnerRegistrationRepository.existsByTaxNumber(request.getTaxNumber())) {
            throw new AppException(ErrorCode.TAX_NUMBER_ALREADY_EXISTS);
        }
        if (accountRepository.existsByEmail(request.getContactPersonEmail())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
    }

    private void approvePartnerRegistration(PartnerRegistration partnerRegistration, UpdatePartnerRegistrationRequest request) {
        partnerRegistration.setStatus(RequestStatus.APPROVED);
        partnerRegistration.setReviewedBy(request.getReviewerId());
        partnerRegistration.setReviewedAt(LocalDateTime.now());
        partnerRegistration.setApprovalNotes(request.getApprovalNotes());
        partnerRegistration.setRejectionReason(null);
        PartnerRegistration savedRegistration = partnerRegistrationRepository.save(partnerRegistration);
        Account account = accountRepository.findAccountByEmail(savedRegistration.getContactPersonEmail())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        account.setStatus(AccountStatus.ACTIVE);
        account.setPartner(createPartner(savedRegistration));
        log.info("Partner created with ID: {} for registration ID: {}", account.getPartner().getId(), savedRegistration.getId());
        accountRepository.save(account);
        createWallet(account.getPartner().getId());
    }

    private void createWallet(Long partnerId) {
        try {
            paymentClient.createWallet(new CreateWalletRequest(partnerId, "PARTNER"));
        } catch (Exception e) {
            log.error("Failed to create wallet for partner ID: {}", partnerId, e);
        }
    }

    private void rejectPartnerRegistration(PartnerRegistration partnerRegistration, UpdatePartnerRegistrationRequest request) {
        partnerRegistration.setStatus(RequestStatus.REJECTED);
        partnerRegistration.setReviewedBy(request.getReviewerId());
        partnerRegistration.setReviewedAt(LocalDateTime.now());
        partnerRegistration.setRejectionReason(request.getRejectionReason());
        partnerRegistration.setApprovalNotes(null);
    }

    private Partner createPartner(PartnerRegistration updatedPartnerRegistration) {
        return partnerRepository.save(
                Partner.builder()
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
                        .build()
        );
    }

    private String generateRandomToken() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));

    }

    private PartnerRegistrationResponse convertToResponseWithPresignedUrl(PartnerRegistration partnerRegistration) {
        PartnerRegistrationResponse response = mapper.toDto(partnerRegistration);

        if (response.getBusinessLicenseFileUrl() != null && !response.getBusinessLicenseFileUrl().isEmpty()) {
            String presignedUrl = s3Service.generatePresignedUrl(response.getBusinessLicenseFileUrl());
            response.setBusinessLicenseFileUrl(presignedUrl);
        }

        return response;
    }
}

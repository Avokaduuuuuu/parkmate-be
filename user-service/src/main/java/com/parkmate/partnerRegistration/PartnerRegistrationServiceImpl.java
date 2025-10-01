package com.parkmate.partnerRegistration;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.common.enums.RequestStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.email.EmailService;
import com.parkmate.partnerRegistration.dto.CreatePartnerRegistrationRequest;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationResponse;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationSearchRequest;
import com.parkmate.partnerRegistration.dto.UpdatePartnerRegistrationRequest;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PartnerRegistrationServiceImpl implements PartnerRegistrationService {

    private final PartnerRegistrationRepository partnerRegistrationRepository;
    private final AccountRepository accountRepository;
    private final PartnerRegistrationMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public PartnerRegistrationResponse registerPartner(CreatePartnerRegistrationRequest request) {
        validateDuplicateConstraint(request);

        String verificationToken = UUID.randomUUID().toString();

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

        PartnerRegistration savedEntity = partnerRegistrationRepository.save(partnerRegistration);

        try {
            emailService.sendVerificationEmail(
                    savedAccount.getEmail(),
                    verificationToken,
                    request.getContactPersonName()
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
        if (accountRepository.existsByEmail(request.getContactPersonEmail())) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
    }
}

package com.parkmate.partnerRegistration;

import com.parkmate.common.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface PartnerRegistrationRepository extends JpaRepository<PartnerRegistration, Long>, QuerydslPredicateExecutor<PartnerRegistration> {

    boolean existsByTaxNumber(String taxNumber);

    Optional<PartnerRegistration> findByContactPersonEmail(String contactPersonEmail);

    Long countPartnerRegistrationByStatus(RequestStatus status);
}

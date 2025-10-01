package com.parkmate.partnerRegistration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PartnerRegistrationRepository extends JpaRepository<PartnerRegistration, Long>, QuerydslPredicateExecutor<PartnerRegistration> {

    boolean existsByTaxNumber(String taxNumber);

}

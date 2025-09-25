package com.parkmate.repository;

import com.parkmate.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long>, QuerydslPredicateExecutor<Partner> {
    boolean existsByTaxNumber(String taxNumber);

}


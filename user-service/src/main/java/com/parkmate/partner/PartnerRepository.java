package com.parkmate.partner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long>, QuerydslPredicateExecutor<Partner> {
    boolean existsByTaxNumber(String taxNumber);

    @Query("SELECT p.taxNumber FROM Partner p")
    List<String> findAllTaxNumbers();


}


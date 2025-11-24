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

    @Query("SELECT p.id FROM Partner p JOIN p.accounts a WHERE a.id = :accountId")
    Long findPartnerIdByAccountId(Long accountId);

    @Query("SELECT " +
            "COUNT(p) AS total, " +
            "COALESCE(COUNT(CASE WHEN p.status = 'APPROVED' THEN 1 END), 0)  as activeTotal," +
            "COALESCE(COUNT(CASE WHEN p.status = 'SUSPENDED' THEN 1 END), 0)  as suspendedTotal " +
            "FROM Partner p")
    PlatformPartnerProjection getPlatformPartnerStatistic();
}


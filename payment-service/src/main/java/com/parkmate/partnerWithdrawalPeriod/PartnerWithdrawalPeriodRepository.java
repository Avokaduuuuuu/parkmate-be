package com.parkmate.partnerWithdrawalPeriod;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerWithdrawalPeriodRepository extends JpaRepository<PartnerWithdrawalPeriod, Long>, JpaSpecificationExecutor<PartnerWithdrawalPeriod> {

    /**
     * Find withdrawal period for a specific parking lot in a date range
     */
    Optional<PartnerWithdrawalPeriod> findByLotIdAndPeriodStartDateAndPeriodEndDate(
            Long lotId,
            LocalDate periodStartDate,
            LocalDate periodEndDate
    );

    /**
     * Find all withdrawal periods for a parking lot
     */
    List<PartnerWithdrawalPeriod> findByLotId(Long lotId);

    /**
     * Find all withdrawal periods for a partner (across all their parking lots)
     */
    List<PartnerWithdrawalPeriod> findByPartnerId(Long partnerId);

    /**
     * Find all withdrawal periods for a partner in a specific date range
     */
    List<PartnerWithdrawalPeriod> findByPartnerIdAndPeriodStartDateAndPeriodEndDate(
            Long partnerId,
            LocalDate periodStartDate,
            LocalDate periodEndDate
    );

    /**
     * Find all withdrawal periods in a specific date range (for all lots)
     */
    List<PartnerWithdrawalPeriod> findByPeriodStartDateAndPeriodEndDate(
            LocalDate periodStartDate,
            LocalDate periodEndDate
    );

    List<PartnerWithdrawalPeriod> findByPartnerIdAndIsWithdrawnFalseOrderByPeriodStartDateDesc(Long partnerId);

    List<PartnerWithdrawalPeriod> findByPartnerIdAndLotIdAndIsWithdrawnFalseOrderByPeriodStartDateDesc(Long partnerId, Long lotId);
}

package com.parkmate.partnerWithdrawalPeriod;

import com.parkmate.partnerWithdrawalPeriod.dto.request.UpdatePartnerWithdrawalPeriodRequest;
import com.parkmate.partnerWithdrawalPeriod.dto.response.PartnerWithdrawalPeriodResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PartnerWithdrawalPeriodService {

    List<PartnerWithdrawalPeriodResponse> createMonthlyWithdrawalPeriods();

    Page<PartnerWithdrawalPeriodResponse> getAllPeriods(
            int page, int size, String sortBy, String sortOrder,
            PartnerWithdrawalPeriodFilterParams filterParams
    );

    PartnerWithdrawalPeriodResponse getPeriodById(Long id);

    PartnerWithdrawalPeriodResponse updatePeriod(Long id, UpdatePartnerWithdrawalPeriodRequest request);

    void deletePeriod(Long id);
}

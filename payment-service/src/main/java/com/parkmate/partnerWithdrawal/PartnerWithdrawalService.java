package com.parkmate.partnerWithdrawal;

import com.parkmate.partnerWithdrawal.dto.CreateWithdrawalRequest;
import com.parkmate.partnerWithdrawal.dto.UpdateWithdrawalRequest;
import com.parkmate.partnerWithdrawal.dto.WithdrawalResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PartnerWithdrawalService {
    WithdrawalResponse createWithdrawal(Long partnerId, CreateWithdrawalRequest request);
    Page<WithdrawalResponse> getAllWithdrawals(int page, int size, String sortBy, String sortOrder, PartnerWithdrawalFilterParams filterParams);
    WithdrawalResponse getWithdrawalById(UUID id);
    WithdrawalResponse updateWithdrawal(UUID id, UpdateWithdrawalRequest request);
    void deleteWithdrawal(UUID id);
}

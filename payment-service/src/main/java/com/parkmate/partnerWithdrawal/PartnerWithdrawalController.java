package com.parkmate.partnerWithdrawal;

import com.parkmate.common.ApiResponse;
import com.parkmate.partnerWithdrawal.dto.CreateWithdrawalRequest;
import com.parkmate.partnerWithdrawal.dto.WithdrawalResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-service/partner/withdrawals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Partner Withdrawal", description = "Partner withdrawal management APIs")
public class PartnerWithdrawalController {

    private final PartnerWithdrawalService partnerWithdrawalService;

    @PostMapping("/request")
    public ApiResponse<WithdrawalResponse> requestWithdrawal(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader,
            @Valid @RequestBody CreateWithdrawalRequest request
    ) {
        Long partnerId = Long.parseLong(userIdHeader);
        WithdrawalResponse response = partnerWithdrawalService.createWithdrawal(partnerId, request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<org.springframework.data.domain.Page<WithdrawalResponse>> getAllWithdrawals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "requestedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            PartnerWithdrawalFilterParams filterParams
    ) {
        org.springframework.data.domain.Page<WithdrawalResponse> withdrawals =
                partnerWithdrawalService.getAllWithdrawals(page, size, sortBy, sortOrder, filterParams);
        return ApiResponse.success(withdrawals);
    }

    @GetMapping("/{id}")
    public ApiResponse<WithdrawalResponse> getWithdrawalById(
            @PathVariable UUID id
    ) {
        WithdrawalResponse response = partnerWithdrawalService.getWithdrawalById(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<WithdrawalResponse> updateWithdrawal(
            @PathVariable UUID id,
            @Valid @RequestBody com.parkmate.partnerWithdrawal.dto.UpdateWithdrawalRequest request
    ) {
        WithdrawalResponse response = partnerWithdrawalService.updateWithdrawal(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWithdrawal(
            @PathVariable UUID id
    ) {
        partnerWithdrawalService.deleteWithdrawal(id);
        return ApiResponse.success(null);
    }
}

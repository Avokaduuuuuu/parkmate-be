package com.parkmate.partnerWithdrawalPeriod;

import com.parkmate.common.ApiResponse;
import com.parkmate.partnerWithdrawalPeriod.dto.request.UpdatePartnerWithdrawalPeriodRequest;
import com.parkmate.partnerWithdrawalPeriod.dto.response.PartnerWithdrawalPeriodResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/partner/withdrawal-periods")
@RequiredArgsConstructor
@Tag(name = "Partner Withdrawal Period", description = "Partner withdrawal period management APIs")
public class PartnerWithdrawalPeriodController {

    private final PartnerWithdrawalPeriodService partnerWithdrawalPeriodService;

    @GetMapping
    public ApiResponse<Page<PartnerWithdrawalPeriodResponse>> getAllPeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "periodStartDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            PartnerWithdrawalPeriodFilterParams filterParams
    ) {
        Page<PartnerWithdrawalPeriodResponse> periods =
                partnerWithdrawalPeriodService.getAllPeriods(page, size, sortBy, sortOrder, filterParams);
        return ApiResponse.success(periods);
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnerWithdrawalPeriodResponse> getPeriodById(
            @PathVariable Long id
    ) {
        PartnerWithdrawalPeriodResponse response = partnerWithdrawalPeriodService.getPeriodById(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<PartnerWithdrawalPeriodResponse> updatePeriod(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartnerWithdrawalPeriodRequest request
    ) {
        PartnerWithdrawalPeriodResponse response = partnerWithdrawalPeriodService.updatePeriod(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePeriod(
            @PathVariable Long id
    ) {
        partnerWithdrawalPeriodService.deletePeriod(id);
        return ApiResponse.success(null);
    }
}

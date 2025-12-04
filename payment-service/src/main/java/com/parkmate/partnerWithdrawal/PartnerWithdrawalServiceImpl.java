package com.parkmate.partnerWithdrawal;

import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.partnerWithdrawal.dto.CreateWithdrawalRequest;
import com.parkmate.partnerWithdrawal.dto.UpdateWithdrawalRequest;
import com.parkmate.partnerWithdrawal.dto.WithdrawalPeriodSummary;
import com.parkmate.partnerWithdrawal.dto.WithdrawalResponse;
import com.parkmate.partnerWithdrawalPeriod.PartnerWithdrawalPeriod;
import com.parkmate.partnerWithdrawalPeriod.PartnerWithdrawalPeriodMapper;
import com.parkmate.partnerWithdrawalPeriod.PartnerWithdrawalPeriodRepository;
import com.parkmate.payos.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v1.payouts.Payout;
import vn.payos.model.v1.payouts.PayoutRequests;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerWithdrawalServiceImpl implements PartnerWithdrawalService {

    private final PartnerWithdrawalRepository partnerWithdrawalRepository;
    private final PartnerWithdrawalPeriodRepository periodRepository;
    private final PartnerWithdrawalPeriodMapper periodMapper;
    private final PayOSService payOSService;


    @Override
    @Transactional
    public WithdrawalResponse createWithdrawal(Long partnerId, CreateWithdrawalRequest request) {
        log.info("Creating withdrawal for partner {} with {} periods", partnerId, request.getPeriodIds().size());

        List<PartnerWithdrawalPeriod> periods = validateAndGetPeriods(partnerId, request);

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalReservation = BigDecimal.ZERO;
        BigDecimal totalSubscription = BigDecimal.ZERO;
        BigDecimal totalWalkIn = BigDecimal.ZERO;

        List<WithdrawalPeriodSummary> periodSummaries = new ArrayList<>();

        for (PartnerWithdrawalPeriod period : periods) {
            totalGross = totalGross.add(period.getGrossRevenue());
            totalFee = totalFee.add(period.getPlatformFee());
            totalNet = totalNet.add(period.getNetRevenue());
            totalReservation = totalReservation.add(period.getReservationRevenue());
            totalSubscription = totalSubscription.add(period.getSubscriptionRevenue());
            totalWalkIn = totalWalkIn.add(period.getWalkInRevenue());

            periodSummaries.add(WithdrawalPeriodSummary.builder()
                    .start(period.getPeriodStartDate().toString())
                    .end(period.getPeriodEndDate().toString())
                    .amount(period.getNetRevenue())
                    .build());
        }

        PartnerWithdrawal withdrawal = new PartnerWithdrawal();
        withdrawal.setId(UUID.randomUUID());
        withdrawal.setLotId(request.getLotId());
        withdrawal.setPartnerId(partnerId);
        withdrawal.setPeriods(convertPeriodsToJson(periodSummaries));
        withdrawal.setTotalGrossRevenue(totalGross);
        withdrawal.setTotalPlatformFee(totalFee);
        withdrawal.setNetAmount(totalNet);
        withdrawal.setTotalAmountReservation(totalReservation);
        withdrawal.setTotalAmountSubscription(totalSubscription);
        withdrawal.setTotalAmountWalkIn(totalWalkIn);
        withdrawal.setStatus(WithdrawalStatus.PROCESSING);
        withdrawal.setRequestedAt(LocalDateTime.now().plusHours(7));
        withdrawal.setCreatedAt(LocalDateTime.now().plusHours(7));
        withdrawal.setUpdatedAt(LocalDateTime.now().plusHours(7));

        PartnerWithdrawal savedWithdrawal = partnerWithdrawalRepository.save(withdrawal);

        try {
            processPayment(savedWithdrawal, partnerId, request);

            for (PartnerWithdrawalPeriod period : periods) {
                period.setIsWithdrawn(true);
                period.setWithdrawnAt(LocalDateTime.now().plusHours(7));
                period.setWithdrawalId(savedWithdrawal.getId());
            }
            periodRepository.saveAll(periods);

            savedWithdrawal.setStatus(WithdrawalStatus.COMPLETED);
            savedWithdrawal.setCompletedAt(LocalDateTime.now().plusHours(7));
            savedWithdrawal = partnerWithdrawalRepository.save(savedWithdrawal);

            log.info("Withdrawal {} completed successfully", savedWithdrawal.getId());

        } catch (Exception e) {
            log.error("Failed to process withdrawal {}", savedWithdrawal.getId(), e);
            savedWithdrawal.setStatus(WithdrawalStatus.FAILED);
            savedWithdrawal.setFailureReason(e.getMessage());
            partnerWithdrawalRepository.save(savedWithdrawal);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, e.getMessage());
        }

        return toResponse(savedWithdrawal, periodSummaries);
    }

    @Override
    public Page<WithdrawalResponse> getAllWithdrawals(int page, int size, String sortBy, String sortOrder, PartnerWithdrawalFilterParams filterParams) {
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        Page<PartnerWithdrawal> withdrawals = partnerWithdrawalRepository.findAll(
                PartnerWithdrawalSpecification.filterBy(filterParams),
                pageable
        );

        return withdrawals.map(w -> toResponse(w, extractPeriodSummaries(w)));
    }

    @Override
    public WithdrawalResponse getWithdrawalById(UUID id) {
        PartnerWithdrawal withdrawal = partnerWithdrawalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        return toResponse(withdrawal, extractPeriodSummaries(withdrawal));
    }

    @Override
    @Transactional
    public WithdrawalResponse updateWithdrawal(UUID id, UpdateWithdrawalRequest request) {
        PartnerWithdrawal withdrawal = partnerWithdrawalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (request.getStatus() != null) {
            withdrawal.setStatus(request.getStatus());
        }
        if (request.getFailureReason() != null) {
            withdrawal.setFailureReason(request.getFailureReason());
        }
        if (request.getDescription() != null) {
            withdrawal.setDescription(request.getDescription());
        }

        withdrawal.setUpdatedAt(LocalDateTime.now().plusHours(7));
        PartnerWithdrawal updated = partnerWithdrawalRepository.save(withdrawal);

        return toResponse(updated, extractPeriodSummaries(updated));
    }

    @Override
    @Transactional
    public void deleteWithdrawal(UUID id) {
        PartnerWithdrawal withdrawal = partnerWithdrawalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (withdrawal.getStatus() == WithdrawalStatus.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cannot delete completed withdrawal");
        }

        partnerWithdrawalRepository.deleteById(id);
    }

    private List<PartnerWithdrawalPeriod> validateAndGetPeriods(Long partnerId, CreateWithdrawalRequest request) {
        List<PartnerWithdrawalPeriod> periods = periodRepository.findAllById(request.getPeriodIds());

        if (periods.size() != request.getPeriodIds().size()) {
            throw new AppException(ErrorCode.WITHDRAWAL_PERIOD_NOT_FOUND);
        }

        for (PartnerWithdrawalPeriod period : periods) {
            if (!period.getPartnerId().equals(partnerId)) {
                throw new AppException(ErrorCode.INVALID_PERIOD_OWNER);
            }
            if (!period.getLotId().equals(request.getLotId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Period does not belong to specified lot");
            }
            if (period.getIsWithdrawn()) {
                throw new AppException(ErrorCode.PERIOD_ALREADY_WITHDRAWN);
            }
        }

        return periods;
    }

    private void processPayment(PartnerWithdrawal withdrawal, Long partnerId, CreateWithdrawalRequest request) {
        try {
            // Generate unique reference ID for this withdrawal
            String referenceId = "PARTNER_WITHDRAW_" + withdrawal.getId();

            // Convert BigDecimal to Long (VND, no decimals)
            Long amountInVnd = withdrawal.getNetAmount().longValue();

            log.info("Creating PayOS payout for partner {} - referenceId: {}, amount: {}, bank: {}",
                partnerId, referenceId, amountInVnd, request.getBankCode());

            // Create PayOS payout request
            PayoutRequests payoutRequest = PayoutRequests.builder()
                    .referenceId(referenceId)
                    .amount(amountInVnd)
                    .toAccountNumber(request.getBankAccountNumber())
                    .toBin(request.getBankCode())
                    .description(String.format("Partner withdrawal - Lot #%d", withdrawal.getLotId()))
                    .build();

            Payout payout = payOSService.retrievePayout(payoutRequest);

            if (payout == null) {
                log.error("PayOS returned null payout response for withdrawal: {}", withdrawal.getId());
                throw new AppException(ErrorCode.PAYOUT_CREATION_FAILED, "PayOS service unavailable");
            }

            // Store PayOS batch ID and bank info in metadata for tracking
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("payoutId", payout.getId());
            metadata.put("referenceId", referenceId);
            metadata.put("bankAccountNumber", request.getBankAccountNumber());
            metadata.put("bankAccountName", request.getBankAccountName());
            metadata.put("bankCode", request.getBankCode());
            withdrawal.setMetadata(metadata);

            // Store external transaction ID
            withdrawal.setExternalTransactionId(payout.getId());
            withdrawal.setProcessedAt(LocalDateTime.now().plusHours(7));

            log.info("✓ PayOS payout created successfully - withdrawal: {}, payoutId: {}, referenceId: {}",
                withdrawal.getId(), payout.getId(), referenceId);

        } catch (Exception e) {
            log.error("Failed to create PayOS payout for withdrawal: {}", withdrawal.getId(), e);
            throw new AppException(ErrorCode.PAYOUT_CREATION_FAILED, e.getMessage());
        }
    }

    private Map<String, Object> convertPeriodsToJson(List<WithdrawalPeriodSummary> periods) {
        Map<String, Object> map = new HashMap<>();
        map.put("periods", periods);
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<WithdrawalPeriodSummary> extractPeriodSummaries(PartnerWithdrawal withdrawal) {
        if (withdrawal.getPeriods() == null) {
            return new ArrayList<>();
        }

        Object periodsObj = withdrawal.getPeriods().get("periods");
        if (periodsObj instanceof List) {
            List<Map<String, Object>> periodsList = (List<Map<String, Object>>) periodsObj;
            return periodsList.stream()
                    .map(p -> WithdrawalPeriodSummary.builder()
                            .start((String) p.get("start"))
                            .end((String) p.get("end"))
                            .amount(new BigDecimal(p.get("amount").toString()))
                            .build())
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private WithdrawalResponse toResponse(PartnerWithdrawal withdrawal, List<WithdrawalPeriodSummary> periods) {
        return WithdrawalResponse.builder()
                .id(withdrawal.getId())
                .lotId(withdrawal.getLotId())
                .partnerId(withdrawal.getPartnerId())
                .periods(periods)
                .totalGrossRevenue(withdrawal.getTotalGrossRevenue())
                .totalPlatformFee(withdrawal.getTotalPlatformFee())
                .netAmount(withdrawal.getNetAmount())
                .totalAmountReservation(withdrawal.getTotalAmountReservation())
                .totalAmountSubscription(withdrawal.getTotalAmountSubscription())
                .totalAmountWalkIn(withdrawal.getTotalAmountWalkIn())
                .status(withdrawal.getStatus())
                .requestedAt(withdrawal.getRequestedAt())
                .processedAt(withdrawal.getProcessedAt())
                .completedAt(withdrawal.getCompletedAt())
                .failureReason(withdrawal.getFailureReason())
                .description(withdrawal.getDescription())
                .externalTransactionId(withdrawal.getExternalTransactionId())
                .build();
    }
}

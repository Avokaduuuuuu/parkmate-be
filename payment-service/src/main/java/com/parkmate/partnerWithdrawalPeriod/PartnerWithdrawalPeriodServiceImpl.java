package com.parkmate.partnerWithdrawalPeriod;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.UserServiceClient;
import com.parkmate.client.dto.ParkingLotBasicInfo;
import com.parkmate.client.dto.RevenueWithCount;
import com.parkmate.common.ApiResponse;
import com.parkmate.common.PaginationUtil;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.partnerWithdrawalPeriod.dto.request.UpdatePartnerWithdrawalPeriodRequest;
import com.parkmate.partnerWithdrawalPeriod.dto.response.PartnerWithdrawalPeriodResponse;
import com.parkmate.systemConfig.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnerWithdrawalPeriodServiceImpl implements PartnerWithdrawalPeriodService {

    private final PartnerWithdrawalPeriodRepository partnerWithdrawalPeriodRepository;
    private final PartnerWithdrawalPeriodMapper partnerWithdrawalPeriodMapper;
    private final ParkingLotClient parkingLotClient;
    private final UserServiceClient userServiceClient;
    private final SystemConfigService systemConfigService;

    @Override
    @Transactional
    public List<PartnerWithdrawalPeriodResponse> createMonthlyWithdrawalPeriods() {
        log.info("=== Starting monthly withdrawal period creation ===");

        Integer cutoffDay = systemConfigService.getWithdrawalCutoffDay();
        log.info("Using withdrawal cutoff day: {}", cutoffDay);

        LocalDate now = LocalDate.now();

        int currentMonthLength = now.lengthOfMonth();
        int actualCutoffDay = Math.min(cutoffDay, currentMonthLength);
        LocalDate currentMonthCutoff = now.withDayOfMonth(actualCutoffDay);

        LocalDate periodEndDate;
        if (now.getDayOfMonth() < actualCutoffDay) {
            periodEndDate = currentMonthCutoff.minusMonths(1);
        } else {
            periodEndDate = currentMonthCutoff;
        }

        LocalDate periodStartDate = periodEndDate.minusMonths(1);

        log.info("Creating withdrawal periods for: {} to {}", periodStartDate, periodEndDate);

        LocalDateTime fromDateTime = periodStartDate.atStartOfDay();
        LocalDateTime toDateTime = periodEndDate.atTime(23, 59, 59);

        String fromStr = fromDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String toStr = toDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        List<ParkingLotBasicInfo> allParkingLots = getAllParkingLots();
        log.info("Found {} parking lots to process", allParkingLots.size());

        List<PartnerWithdrawalPeriodResponse> responses = new ArrayList<>();

        for (ParkingLotBasicInfo lot : allParkingLots) {
            try {
                PartnerWithdrawalPeriodResponse response = createWithdrawalPeriodForParkingLot(
                        lot, periodStartDate, periodEndDate, fromStr, toStr
                );
                if (response != null) {
                    responses.add(response);
                }
            } catch (Exception e) {
                log.error("Failed to create withdrawal period for lot {}", lot.getId(), e);
            }
        }

        log.info("=== Completed monthly withdrawal period creation. Created {} periods ===", responses.size());
        return responses;
    }

    private PartnerWithdrawalPeriodResponse createWithdrawalPeriodForParkingLot(
            ParkingLotBasicInfo lot,
            LocalDate periodStartDate,
            LocalDate periodEndDate,
            String fromStr,
            String toStr
    ) {
        Long lotId = lot.getId();
        Long partnerId = lot.getPartnerId();

        log.info("Processing parking lot: {} (Partner: {})", lotId, partnerId);

        // Check if period already exists for this parking lot
        var existing = partnerWithdrawalPeriodRepository
                .findByLotIdAndPeriodStartDateAndPeriodEndDate(lotId, periodStartDate, periodEndDate);

        if (existing.isPresent()) {
            log.info("Withdrawal period already exists for lot {} in period {} to {}",
                    lotId, periodStartDate, periodEndDate);
            return existing.map(partnerWithdrawalPeriodMapper::toResponse).get();
        }

        // Calculate revenues and counts for this parking lot
        BigDecimal totalReservation = BigDecimal.ZERO;
        BigDecimal totalSubscription = BigDecimal.ZERO;
        BigDecimal totalWalkIn = BigDecimal.ZERO;

        Long reservationCount = 0L;
        Long subscriptionCount = 0L;
        Long walkInCount = 0L;

        try {
            // Get MEMBER + RESERVATION revenue and count (from sessions)
            ApiResponse<RevenueWithCount> reservationData =
                    parkingLotClient.getMemberReservationRevenue(lotId, fromStr, toStr);
            if (reservationData.data() != null) {
                totalReservation = reservationData.data().getRevenue();
                reservationCount = reservationData.data().getCount();
            }

            // Get penalty revenue from cancelled/expired reservations (no sessions created)
            // Add this to reservation revenue
            ApiResponse<RevenueWithCount> penaltyData =
                    userServiceClient.getReservationPenaltyRevenue(lotId, fromStr, toStr);
            if (penaltyData.data() != null) {
                totalReservation = totalReservation.add(penaltyData.data().getRevenue());
                reservationCount = reservationCount + penaltyData.data().getCount();
            }

            // Get MEMBER + WALK_IN revenue and count (from sessions)
            ApiResponse<RevenueWithCount> walkInData =
                    parkingLotClient.getMemberWalkInRevenue(lotId, fromStr, toStr);
            if (walkInData.data() != null) {
                totalWalkIn = walkInData.data().getRevenue();
                walkInCount = walkInData.data().getCount();
            }

            // Get subscription revenue and count
            ApiResponse<RevenueWithCount> subscriptionData =
                    userServiceClient.getSubscriptionRevenue(lotId, fromStr, toStr);
            if (subscriptionData.data() != null) {
                totalSubscription = subscriptionData.data().getRevenue();
                subscriptionCount = subscriptionData.data().getCount();
            }

            log.debug("Lot {} - Reservation: {} ({}), WalkIn: {} ({}), Subscription: {} ({})",
                    lotId, totalReservation, reservationCount, totalWalkIn, walkInCount,
                    totalSubscription, subscriptionCount);

        } catch (Exception e) {
            log.error("Failed to fetch revenue and counts for lot {}", lotId, e);
            // Continue with zero values or throw based on requirement
        }

        // Create period label showing the date range (e.g., "2024-01-18 to 2024-02-18")
        String periodLabel = String.format("%s to %s",
                periodStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                periodEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Calculate gross revenue (penalty revenue already added to totalReservation)
        BigDecimal grossRevenue = totalReservation.add(totalSubscription).add(totalWalkIn);
        BigDecimal platformFeePercentage = systemConfigService.getPlatformFeePercentage();
        BigDecimal platformFee = grossRevenue.multiply(platformFeePercentage);
        BigDecimal netRevenue = grossRevenue.subtract(platformFee);

        Integer totalSessions = Math.toIntExact(reservationCount + subscriptionCount + walkInCount);

        PartnerWithdrawalPeriod period = new PartnerWithdrawalPeriod();
        period.setLotId(lotId);
        period.setPartnerId(partnerId);
        period.setPeriodStartDate(periodStartDate);
        period.setPeriodEndDate(periodEndDate);
        period.setPeriodLabel(periodLabel);
        period.setIsWithdrawn(false);
        period.setReservationRevenue(totalReservation);
        period.setSubscriptionRevenue(totalSubscription);
        period.setWalkInRevenue(totalWalkIn);
        period.setGrossRevenue(grossRevenue);
        period.setPlatformFee(platformFee);
        period.setNetRevenue(netRevenue);
        period.setTotalSessions(totalSessions);
        period.setReservationSessions(Math.toIntExact(reservationCount));
        period.setSubscriptionSessions(Math.toIntExact(subscriptionCount));
        period.setWalkInSessions(Math.toIntExact(walkInCount));
        period.setCreatedAt(LocalDateTime.now().plusHours(7));
        period.setUpdatedAt(LocalDateTime.now().plusHours(7));

        PartnerWithdrawalPeriod saved = partnerWithdrawalPeriodRepository.save(period);

        log.info("Created withdrawal period for lot {} - Reservation: {} ({}), WalkIn: {} ({}), Subscription: {} ({}), Total: {} ({})",
                lotId, totalReservation, reservationCount, totalWalkIn, walkInCount,
                totalSubscription, subscriptionCount, grossRevenue, totalSessions);

        return partnerWithdrawalPeriodMapper.toResponse(saved);
    }

    /**
     * Get all parking lots across all partners
     * This requires a new endpoint in parking-lot-service: GET /internal/parking-lots/all
     */
    private List<ParkingLotBasicInfo> getAllParkingLots() {
        try {
            // Call parking-lot-service to get all parking lots
            ApiResponse<List<ParkingLotBasicInfo>> response = parkingLotClient.getAllParkingLots();
            List<ParkingLotBasicInfo> parkingLots = response.data();

            if (parkingLots == null || parkingLots.isEmpty()) {
                log.warn("No parking lots found in the system");
                return new ArrayList<>();
            }

            log.info("Retrieved {} parking lots from parking-lot-service", parkingLots.size());
            return parkingLots;

        } catch (Exception e) {
            log.error("Failed to retrieve parking lots from parking-lot-service", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Page<PartnerWithdrawalPeriodResponse> getAllPeriods(
            int page, int size, String sortBy, String sortOrder,
            PartnerWithdrawalPeriodFilterParams filterParams
    ) {
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        Page<PartnerWithdrawalPeriod> periods = partnerWithdrawalPeriodRepository.findAll(
                PartnerWithdrawalPeriodSpecification.filterBy(filterParams),
                pageable
        );

        return periods.map(partnerWithdrawalPeriodMapper::toResponse);
    }

    @Override
    public PartnerWithdrawalPeriodResponse getPeriodById(Long id) {
        PartnerWithdrawalPeriod period = partnerWithdrawalPeriodRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_PERIOD_NOT_FOUND));
        return partnerWithdrawalPeriodMapper.toResponse(period);
    }

    @Override
    @Transactional
    public PartnerWithdrawalPeriodResponse updatePeriod(Long id, UpdatePartnerWithdrawalPeriodRequest request) {
        PartnerWithdrawalPeriod period = partnerWithdrawalPeriodRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_PERIOD_NOT_FOUND));

        if (request.getGrossRevenue() != null) {
            period.setGrossRevenue(request.getGrossRevenue());
        }
        if (request.getPlatformFee() != null) {
            period.setPlatformFee(request.getPlatformFee());
        }
        if (request.getNetRevenue() != null) {
            period.setNetRevenue(request.getNetRevenue());
        }
        if (request.getWalkInRevenue() != null) {
            period.setWalkInRevenue(request.getWalkInRevenue());
        }
        if (request.getReservationRevenue() != null) {
            period.setReservationRevenue(request.getReservationRevenue());
        }
        if (request.getSubscriptionRevenue() != null) {
            period.setSubscriptionRevenue(request.getSubscriptionRevenue());
        }
        if (request.getTotalSessions() != null) {
            period.setTotalSessions(request.getTotalSessions());
        }
        if (request.getWalkInSessions() != null) {
            period.setWalkInSessions(request.getWalkInSessions());
        }
        if (request.getReservationSessions() != null) {
            period.setReservationSessions(request.getReservationSessions());
        }
        if (request.getSubscriptionSessions() != null) {
            period.setSubscriptionSessions(request.getSubscriptionSessions());
        }

        period.setUpdatedAt(LocalDateTime.now().plusHours(7));
        PartnerWithdrawalPeriod updated = partnerWithdrawalPeriodRepository.save(period);

        return partnerWithdrawalPeriodMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletePeriod(Long id) {
        PartnerWithdrawalPeriod period = partnerWithdrawalPeriodRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_PERIOD_NOT_FOUND));

        if (period.getIsWithdrawn()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cannot delete withdrawn period");
        }

        partnerWithdrawalPeriodRepository.deleteById(id);
    }
}

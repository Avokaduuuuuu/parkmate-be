package com.parkmate.partnerWithdrawalPeriod;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.UserServiceClient;
import com.parkmate.client.dto.ParkingLotBasicInfo;
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
import java.time.YearMonth;
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

        // Calculate previous month date range
        LocalDate now = LocalDate.now();
        YearMonth lastMonth = YearMonth.from(now).minusMonths(1);
        LocalDate periodStartDate = lastMonth.atDay(1);
        LocalDate periodEndDate = lastMonth.atEndOfMonth();

        log.info("Creating withdrawal periods for: {} to {}", periodStartDate, periodEndDate);

        // Convert to LocalDateTime for queries (start of day and end of day)
        LocalDateTime fromDateTime = periodStartDate.atStartOfDay();
        LocalDateTime toDateTime = periodEndDate.atTime(23, 59, 59);

        String fromStr = fromDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String toStr = toDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Get all parking lots (across all partners)
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
                // Continue with other lots
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
            return partnerWithdrawalPeriodMapper.toResponse(existing.get());
        }

        // Calculate revenues for this parking lot
        BigDecimal totalReservation = BigDecimal.ZERO;
        BigDecimal totalSubscription = BigDecimal.ZERO;
        BigDecimal totalWalkIn = BigDecimal.ZERO;

        try {
            // Get MEMBER + RESERVATION revenue
            ApiResponse<BigDecimal> reservationRevenue =
                    parkingLotClient.getMemberReservationRevenue(lotId, fromStr, toStr);
            if (reservationRevenue.data() != null) {
                totalReservation = reservationRevenue.data();
            }

            // Get MEMBER + WALK_IN revenue
            ApiResponse<BigDecimal> walkInRevenue =
                    parkingLotClient.getMemberWalkInRevenue(lotId, fromStr, toStr);
            if (walkInRevenue.data() != null) {
                totalWalkIn = walkInRevenue.data();
            }

            // Get subscription revenue
            ApiResponse<BigDecimal> subscriptionRevenue =
                    userServiceClient.getSubscriptionRevenue(lotId, fromStr, toStr);
            if (subscriptionRevenue.data() != null) {
                totalSubscription = subscriptionRevenue.data();
            }

            log.debug("Lot {} - Reservation: {}, WalkIn: {}, Subscription: {}",
                    lotId, totalReservation, totalWalkIn, totalSubscription);

        } catch (Exception e) {
            log.error("Failed to fetch revenue for lot {}", lotId, e);
            // Continue with zero values or throw based on requirement
        }

        String periodLabel = periodStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        BigDecimal grossRevenue = totalReservation.add(totalSubscription).add(totalWalkIn);
        BigDecimal platformFeePercentage = systemConfigService.getPlatformFeePercentage();
        BigDecimal platformFee = grossRevenue.multiply(platformFeePercentage);
        BigDecimal netRevenue = grossRevenue.subtract(platformFee);

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
        period.setCreatedAt(LocalDateTime.now());
        period.setUpdatedAt(LocalDateTime.now());

        PartnerWithdrawalPeriod saved = partnerWithdrawalPeriodRepository.save(period);

        log.info("Created withdrawal period for lot {} - Reservation: {}, WalkIn: {}, Subscription: {}",
                lotId, totalReservation, totalWalkIn, totalSubscription);

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

        period.setUpdatedAt(LocalDateTime.now());
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

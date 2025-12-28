package com.parkmate.operationalPayment;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.dto.ParkingLotBasicInfo;
import com.parkmate.device_fee_config.DeviceFeeConfigEntity;
import com.parkmate.device_fee_config.DeviceFeeConfigRepository;
import com.parkmate.device_payment_item.DevicePaymentItemEntity;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.operationalFeeConfig.OperationalFeeConfigEntity;
import com.parkmate.operationalFeeConfig.OperationalFeeConfigRepository;
import com.parkmate.operationalPayment.enums.PaymentStatus;
import com.parkmate.systemConfig.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecurringOperationalPaymentScheduler {

    private final OperationalPaymentRepository operationalPaymentRepository;
    private final OperationalFeeConfigRepository operationalFeeConfigRepository;
    private final ParkingLotClient parkingLotClient;
    private final SystemConfigService systemConfigService;
    private final DeviceFeeConfigRepository deviceFeeConfigRepository;

    /**
     * Task: Create recurring operational payments for ACTIVE parking lots
     * Runs daily at 2:00 AM Vietnam time to check if billing cycle has arrived
     *
     * Logic:
     * - Get OPERATIONAL_FEE_BILLING_CYCLE_DAY from system config (e.g., 30 days)
     * - For each ACTIVE parking lot:
     *   - Check latest paid operational payment
     *   - If billing cycle has passed since last payment, create new payment
     * - New payment status: PENDING
     * - Due date: 30 days from creation
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void createRecurringOperationalPayments() {
        LocalDate today = LocalDate.now();

        log.info("=== [SCHEDULER] Task: Creating recurring operational payments for {} ===", today);

        try {
            // Get billing cycle configuration (in days)
            Integer billingCycleDays = systemConfigService.getOperationalFeeBillingCycleDay();
            if (billingCycleDays == null) {
                log.error("OPERATIONAL_FEE_BILLING_CYCLE_DAY not configured in system_config, skipping");
                return;
            }

            log.info("Using billing cycle: {} days", billingCycleDays);

            // Fetch active operational fee config
            OperationalFeeConfigEntity config = operationalFeeConfigRepository
                    .findActiveConfig(LocalDateTime.now().plusHours(7))
                    .orElse(null);

            if (config == null) {
                log.error("No active operational fee config found, skipping payment creation");
                return;
            }

            // Get all ACTIVE parking lots from parking-lot-service
            var parkingLotsResponse = parkingLotClient.getAllParkingLots();
            if (parkingLotsResponse == null || parkingLotsResponse.data() == null) {
                log.error("Failed to fetch parking lots from parking-lot-service");
                return;
            }

            List<ParkingLotBasicInfo> allParkingLots = parkingLotsResponse.data();
            log.info("Found {} total parking lots", allParkingLots.size());

            int createdCount = 0;
            int skippedCount = 0;

            for (ParkingLotBasicInfo lot : allParkingLots) {
                try {
                    // Check if this lot needs a new payment
                    boolean needsPayment = checkAndCreatePaymentIfNeeded(
                            lot,
                            billingCycleDays,
                            config,
                            today
                    );

                    if (needsPayment) {
                        createdCount++;
                    } else {
                        skippedCount++;
                    }

                } catch (Exception e) {
                    log.error("Error processing lot {}: {}", lot.getId(), e.getMessage(), e);
                }
            }

            log.info("✅ [SCHEDULER] Completed: Created {} payments, Skipped {} lots",
                    createdCount, skippedCount);

        } catch (Exception e) {
            log.error("❌ [SCHEDULER] Fatal error in createRecurringOperationalPayments", e);
        }
    }

    /**
     * Check if parking lot needs a new payment and create if necessary
     *
     * @return true if new payment was created, false if skipped
     */
    private boolean checkAndCreatePaymentIfNeeded(
            ParkingLotBasicInfo lot,
            Integer billingCycleDays,
            OperationalFeeConfigEntity config,
            LocalDate today
    ) {
        log.debug("Checking lot {} ({})", lot.getId(), lot.getName());

        // Get latest payment for this lot (any status)
        var latestPaymentOpt = operationalPaymentRepository.findLatestByLotId(lot.getId());

        if (latestPaymentOpt.isEmpty()) {
            log.info("No previous payment found for lot {} - this should have been created during PENDING_PAYMENT status",
                    lot.getId());
            return false; // Initial payment should be created when lot status changes to PENDING_PAYMENT
        }

        OperationalPaymentEntity latestPayment = latestPaymentOpt.orElseThrow();

        // Only create recurring payment if latest payment is PAID
        if (latestPayment.getPaymentStatus() != PaymentStatus.PAID) {
            log.debug("Latest payment for lot {} is not PAID (status: {}), skipping",
                    lot.getId(), latestPayment.getPaymentStatus());
            return false;
        }

        // Check if billing cycle has passed since last paid payment
        LocalDate lastPaymentEndDate = latestPayment.getBillingEndDate();
        LocalDate nextBillingStartDate = lastPaymentEndDate.plusDays(1);

        // Check if we've reached or passed the next billing cycle start
        if (today.isBefore(nextBillingStartDate)) {
            log.debug("Not yet time for next billing cycle for lot {}. Next start: {}, Today: {}",
                    lot.getId(), nextBillingStartDate, today);
            return false;
        }

        // Create new recurring payment
        return createRecurringPayment(lot, config, nextBillingStartDate, billingCycleDays);
    }

    private boolean createRecurringPayment(
            ParkingLotBasicInfo lot,
            OperationalFeeConfigEntity config,
            LocalDate billingStartDate,
            Integer billingCycleDays
    ) {
        log.info("Creating recurring operational payment for lot {} starting {}",
                lot.getId(), billingStartDate);

        try {
            // Fetch lot area from parking-lot-service (assuming it's in ParkingLotBasicInfo or needs separate call)
            // For now, we'll fetch from latest payment as area shouldn't change
            var latestPayment = operationalPaymentRepository.findLatestByLotId(lot.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
            Double lotAreaSqm = latestPayment.getLotAreaSqm();

            // Calculate billing period
            LocalDate billingEndDate = billingStartDate.plusDays(billingCycleDays);
            LocalDate dueDate = billingStartDate.plusDays(30); // Payment due in 30 days

            // Calculate total fee
            BigDecimal pricePerSqm = BigDecimal.valueOf(config.getPricePerSqm());
            BigDecimal lotArea = BigDecimal.valueOf(lotAreaSqm);

            BigDecimal areaFee = lotArea
                    .multiply(pricePerSqm)
                    .setScale(2, RoundingMode.HALF_UP);

            // Create new payment entity
            OperationalPaymentEntity newPayment = OperationalPaymentEntity.builder()
                    .lotId(lot.getId())
                    .partnerId(lot.getPartnerId())
                    .feeConfigId(config.getId())
                    .lotAreaSqm(lotAreaSqm)
                    .feePerSqm(pricePerSqm)
                    .billingPeriodMonths(config.getBillingPeriodMonths())
                    .billingStartDate(billingStartDate)
                    .billingEndDate(billingEndDate)
                    .areaFee(areaFee)
                    .dueDate(dueDate)
                    .paymentStatus(PaymentStatus.PENDING)
                    .notes(String.format("Recurring operational fee payment (cycle: %d days)", billingCycleDays))
                    .build();


            List<DevicePaymentItemEntity> devicePaymentItemEntities = lot.getDeviceItemPaymentRequests().stream().map(
                    itemReq -> {
                        DeviceFeeConfigEntity deviceFeeConfigEntity = deviceFeeConfigRepository.findByDeviceType(itemReq.deviceType())
                                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_FEE_CONFIG_NOT_FOUND));
                        DevicePaymentItemEntity devicePaymentItemEntity = new DevicePaymentItemEntity();
                        devicePaymentItemEntity.setDeviceType(itemReq.deviceType());
                        devicePaymentItemEntity.setTotalDevice(itemReq.totalDevice());
                        devicePaymentItemEntity.setDeviceFee(deviceFeeConfigEntity.getDeviceFee());
                        devicePaymentItemEntity.setTotalFee(deviceFeeConfigEntity.getDeviceFee().multiply(BigDecimal.valueOf(itemReq.totalDevice())));
                        devicePaymentItemEntity.setOperationalPayment(newPayment);
                        devicePaymentItemEntity.setDeviceFeeConfig(deviceFeeConfigEntity);
                        return devicePaymentItemEntity;
                    }).collect(Collectors.toList());
            newPayment.setDevicePaymentItems(devicePaymentItemEntities);

            BigDecimal deviceFee = devicePaymentItemEntities.stream().map(DevicePaymentItemEntity::getTotalFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalFee = areaFee.add(deviceFee);
            newPayment.setDeviceFee(deviceFee);
            newPayment.setTotalFee(totalFee);
            operationalPaymentRepository.save(newPayment);

            log.info("✅ Created recurring payment #{} for lot {}: {} VND, due {}",
                    newPayment.getId(), lot.getId(), totalFee, dueDate);

            return true;

        } catch (Exception e) {
            log.error("Failed to create recurring payment for lot {}", lot.getId(), e);
            return false;
        }
    }
}

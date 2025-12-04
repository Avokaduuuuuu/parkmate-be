package com.parkmate.operationalPayment;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.operationalFeeConfig.OperationalFeeConfigEntity;
import com.parkmate.operationalFeeConfig.OperationalFeeConfigRepository;
import com.parkmate.operationalPayment.dto.CreateOperationalPaymentRequest;
import com.parkmate.operationalPayment.dto.OperationalPaymentResponse;
import com.parkmate.operationalPayment.dto.OperationalPaymentStatusResponse;
import com.parkmate.operationalPayment.enums.PaymentStatus;
import com.parkmate.payos.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationalPaymentServiceImpl implements OperationalPaymentService {

    private final OperationalPaymentRepository operationalPaymentRepository;
    private final OperationalFeeConfigRepository operationalFeeConfigRepository;
    private final PayOSService payOSService;

    @Override
    @Transactional
    public OperationalPaymentResponse createPaymentForParkingLot(CreateOperationalPaymentRequest request) {
        log.info("Creating operational payment for lot: {}, partner: {}, area: {} sqm",
                request.lotId(), request.partnerId(), request.lotAreaSqm());

        // 1. Fetch active operational fee config
        OperationalFeeConfigEntity config = operationalFeeConfigRepository
                .findActiveConfig(LocalDateTime.now().plusHours(7))
                .orElseThrow(() -> new AppException(ErrorCode.OPERATIONAL_FEE_CONFIG_NOT_FOUND));

        log.debug("Using operational fee config: {} VND/sqm, {} months",
                config.getPricePerSqm(), config.getBillingPeriodMonths());

        // 2. Calculate total fee: lotAreaSqm × pricePerSqm × billingPeriodMonths
        BigDecimal pricePerSqm = BigDecimal.valueOf(config.getPricePerSqm());
        BigDecimal lotArea = BigDecimal.valueOf(request.lotAreaSqm());
        BigDecimal billingMonths = BigDecimal.valueOf(config.getBillingPeriodMonths());

        BigDecimal totalFee = lotArea
                .multiply(pricePerSqm)
                .multiply(billingMonths)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Calculated total fee: {} VND for {} sqm × {} VND/sqm × {} months",
                totalFee, request.lotAreaSqm(), config.getPricePerSqm(), config.getBillingPeriodMonths());

        // 3. Calculate billing period dates
        LocalDate billingStartDate = LocalDate.now();
        LocalDate billingEndDate = billingStartDate.plusMonths(config.getBillingPeriodMonths());
        LocalDate dueDate = billingStartDate.plusDays(30); // Payment due in 30 days

        // 4. Create OperationalPaymentEntity with PENDING status
        OperationalPaymentEntity paymentEntity = OperationalPaymentEntity.builder()
                .lotId(request.lotId())
                .partnerId(request.partnerId())
                .feeConfigId(config.getId())
                .lotAreaSqm(request.lotAreaSqm())
                .feePerSqm(pricePerSqm)
                .billingPeriodMonths(config.getBillingPeriodMonths())
                .billingStartDate(billingStartDate)
                .billingEndDate(billingEndDate)
                .totalFee(totalFee)
                .dueDate(dueDate)
                .paymentStatus(PaymentStatus.PENDING)
                .notes("Initial operational fee payment for parking lot activation")
                .build();

        OperationalPaymentEntity savedPayment = operationalPaymentRepository.save(paymentEntity);
        log.info("Created operational payment entity with ID: {}", savedPayment.getId());

        // 5. Generate PayOS payment link
        CreatePaymentLinkResponse payOSResponse;
        try {
            // Convert BigDecimal to Long (VND amount in cents)
            Long amountInVND = totalFee.longValue();

            // Call PayOS to create operational fee payment
            payOSResponse = payOSService.createOperationalFeePayment(
                    savedPayment.getId(),
                    request.lotId(),
                    amountInVND
            );

            // Update payment entity with PayOS transaction ID
            savedPayment.setPaymentTransactionId(String.valueOf(payOSResponse.getOrderCode()));
            operationalPaymentRepository.save(savedPayment);

            log.info("PayOS payment link created: {}, orderCode: {}",
                    payOSResponse.getCheckoutUrl(), payOSResponse.getOrderCode());

        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for operational payment: {}", savedPayment.getId(), e);

            // Mark payment as cancelled since PayOS link creation failed
            savedPayment.setPaymentStatus(PaymentStatus.CANCELLED);
            savedPayment.setNotes("Payment link generation failed: " + e.getMessage());
            operationalPaymentRepository.save(savedPayment);

            throw new AppException(ErrorCode.PAYOS_PAYMENT_CREATION_FAILED);
        }

        // 6. Return payment response
        return OperationalPaymentResponse.builder()
                .id(savedPayment.getId())
                .lotId(savedPayment.getLotId())
                .partnerId(savedPayment.getPartnerId())
                .lotAreaSqm(savedPayment.getLotAreaSqm())
                .feePerSqm(savedPayment.getFeePerSqm())
                .billingPeriodMonths(savedPayment.getBillingPeriodMonths())
                .totalFee(savedPayment.getTotalFee())
                .paymentStatus(savedPayment.getPaymentStatus())
                .paymentLink(payOSResponse.getCheckoutUrl())
                .qrCode(payOSResponse.getQrCode())
                .dueDate(savedPayment.getDueDate().atStartOfDay())
                .paidAt(savedPayment.getPaidAt())
                .createdAt(savedPayment.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OperationalPaymentStatusResponse getPaymentStatus(Long paymentId) {
        log.info("Getting payment status for operational payment ID: {}", paymentId);

        OperationalPaymentEntity payment = operationalPaymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return OperationalPaymentStatusResponse.builder()
                .id(payment.getId())
                .lotId(payment.getLotId())
                .paymentStatus(payment.getPaymentStatus())
                .totalFee(payment.getTotalFee())
                .dueDate(payment.getDueDate().atStartOfDay())
                .paidAt(payment.getPaidAt())
                .build();
    }

    @Override
    @Transactional
    public void confirmPayment(String paymentTransactionId) {
        log.info("Confirming operational payment with transaction ID: {}", paymentTransactionId);

        OperationalPaymentEntity payment = operationalPaymentRepository
                .findByPaymentTransactionId(paymentTransactionId)
                .orElseThrow(() -> {
                    log.error("Operational payment not found for transaction ID: {}", paymentTransactionId);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("Cannot confirm payment {} - current status: {}",
                    payment.getId(), payment.getPaymentStatus());
            throw new AppException(ErrorCode.INVALID_PAYMENT_DETAILS);
        }
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now().plusHours(7));
        payment.setNotes("Payment confirmed via PayOS webhook");
        operationalPaymentRepository.save(payment);

        log.info("Operational payment {} confirmed successfully for lot: {}",
                payment.getId(), payment.getLotId());

        // Note: Parking lot activation is now handled by PayOS webhook directly
        // to avoid circular dependency between services
    }

    @Override
    @Transactional
    public void cancelPayment(Long paymentId) {
        log.info("Cancelling operational payment ID: {}", paymentId);

        OperationalPaymentEntity payment = operationalPaymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Only cancel if status is PENDING
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("Cannot cancel payment {} - current status: {}",
                    payment.getId(), payment.getPaymentStatus());
            throw new AppException(ErrorCode.INVALID_PAYMENT_DETAILS);
        }

        // Cancel payment on PayOS if paymentTransactionId exists
        if (payment.getPaymentTransactionId() != null && !payment.getPaymentTransactionId().isEmpty()) {
            try {
                Long orderCode = Long.parseLong(payment.getPaymentTransactionId());
                payOSService.cancelPayment(orderCode);
                log.info("Successfully cancelled PayOS payment with orderCode: {}", orderCode);
            } catch (NumberFormatException e) {
                log.error("Invalid payment transaction ID format: {}", payment.getPaymentTransactionId());
                throw new AppException(ErrorCode.INVALID_PAYMENT_DETAILS,
                    "Invalid payment transaction ID format");
            } catch (Exception e) {
                log.error("Failed to cancel PayOS payment: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.CANCEL_FAILED,
                    "Failed to cancel payment with PayOS: " + e.getMessage());
            }
        } else {
            log.warn("No payment transaction ID found for payment {}, skipping PayOS cancellation",
                payment.getId());
        }

        // Update database status
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setNotes("Payment cancelled by user or system");
        operationalPaymentRepository.save(payment);

        log.info("Operational payment {} cancelled successfully", payment.getId());
    }
}
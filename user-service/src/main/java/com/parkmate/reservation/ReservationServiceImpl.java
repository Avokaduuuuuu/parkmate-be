package com.parkmate.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.constants.TransactionConstants;
import com.parkmate.client.dto.request.CreateTransactionRequest;
import com.parkmate.client.dto.response.WalletTransactionResponse;
import com.parkmate.client.enums.TransactionStatus;
import com.parkmate.client.exception.ParkingLotServiceErrorCode;
import com.parkmate.common.dto.ApiResponse;
import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.common.util.QRCodeGenerator;
import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.event.NotificationEventType;
import com.parkmate.mobileDevice.MobileDeviceRepository;
import com.parkmate.reservation.dto.*;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import com.parkmate.user.UserService;
import com.parkmate.vehicle.Vehicle;
import com.parkmate.vehicle.VehicleRepository;
import com.parkmate.vehicle.VehicleService;
import com.parkmate.vehicle.VehicleType;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final PaymentClient paymentClient;
    private final ParkingLotClient parkingLotClient;
    private final ReservationMapper reservationMapper;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final ReservationHoldService reservationHoldService;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, String userId) {

        if (userId != null && request.isOwnedByMe()) {
            long userIdLong = Long.parseLong(userId);
            request.setUserId(userIdLong);
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
        ApiResponse<?> parkingLotResponse = parkingLotClient.getParkingLotName(request.getParkingLotId());
        ApiResponse<?> pricingRuleResponse = parkingLotClient.getPricingRule(request.getParkingLotId(), vehicle.getVehicleType());
        if (parkingLotResponse == null || parkingLotResponse.data() == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.PARKING_NOT_FOUND);
        }

        Object data = parkingLotResponse.data();
        if (data instanceof ParkingLotClient.ParkingLotSimpleDto parkingLot) {
            Integer horizonTime = parkingLot.horizonTime();
            if (horizonTime != null && request.getAssumedStayMinute() < horizonTime) {
                request.setAssumedStayMinute(horizonTime);
            }
        }
        Object pricingRuleData = pricingRuleResponse.data();
        if (pricingRuleData instanceof ParkingLotClient.PricingRuleDto pricingRule) {
            request.setPricingRuleId(pricingRule.id());
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        Reservation reservation = Reservation.builder()
                .user(user)
                .reservedFrom(request.getReservedFrom())
                .initialFee(request.getInitialFee())
                .vehicle(vehicle)
                .parkingLotId(request.getParkingLotId())
                .pricingRuleId(request.getPricingRuleId())
                .status(ReservationStatus.PENDING)
                .assumedStayMinute(request.getAssumedStayMinute())
                .build();

        reservation = reservationRepository.save(reservation);

        try {
            // Process initial payment
            processWalletDeduction(reservation, reservation.getInitialFee(), "Cọc đặt chỗ: " + reservation.getId());

            reservation.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(reservation);

            if (request.getHoldId() != null && !request.getHoldId().isEmpty()) {
                try {
                    reservationHoldService.releaseHold(request.getHoldId());
                    log.info("Released temporary hold {} after successful reservation {}",
                            request.getHoldId(), reservation.getId());
                } catch (Exception holdEx) {
                    log.warn("Failed to release hold {} for reservation {}: {}",
                            request.getHoldId(), reservation.getId(), holdEx.getMessage());
                }
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment for reservation ID: {}", reservation.getId(), e);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment processing failed: " + e.getMessage());
        }

        log.info("🔔 [CREATE-RESERVATION] About to send PENDING notification for reservation: {}", reservation.getId());
        try {
            sendNotificationForStatus(reservation.getId(), ReservationStatus.PENDING);
            log.info("✅ [CREATE-RESERVATION] Notification call completed for reservation: {}", reservation.getId());
        } catch (Exception e) {
            log.error("❌ [CREATE-RESERVATION] Failed to send notification for reservation: {}", reservation.getId(), e);
        }

        return getReservationResponse(reservation);
    }

    private String generateQRCodeContent(Reservation reservation) {
        try {
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("reservationId", reservation.getId());
            qrData.put("qrType", "reservation");
            return objectMapper.writeValueAsString(qrData);
        } catch (Exception e) {
            log.error("Error generating QR code content for reservation ID: {}", reservation.getId(), e);
            // Fallback to simple format
            return String.format("RESERVATION:%d|USER:%d|LOT:%d",
                    reservation.getId(),
                    reservation.getUser().getId(),
                    reservation.getParkingLotId());
        }
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        return null;
    }

    @Override
    @Transactional
    public void cancelReservation(Long id, String userIdHeader) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found: " + id));

        // Check if reservation belongs to the user
        if (userIdHeader != null) {
            Long userId = Long.parseLong(userIdHeader);
            if (!reservation.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.RESERVATION_NOT_BELONG_TO_USER);
            }
        }

        // Check if reservation is already canceled
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new AppException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        // Check if reservation can be canceled (only PENDING or ACTIVE can be canceled)
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new AppException(ErrorCode.RESERVATION_CANNOT_BE_CANCELLED);
        }

        // Check refund policy
        boolean shouldRefund = checkRefundEligibility(reservation);

        // Update reservation status
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        if (shouldRefund) {
            processRefund(reservation);
        } else {
            log.info("Reservation {} cancelled without refund (outside refund window)", reservation.getId());
        }

        // Send notification
        sendNotificationForStatus(reservation.getId(), ReservationStatus.CANCELLED);
    }

    /**
     * Check if reservation is eligible for refund based on cancellation policy
     * Returns true if canceled before the refund deadline
     */
    private boolean checkRefundEligibility(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();

        // Get cancellation refund policy from parking lot
        Integer refundWindowMinutes = null;
        try {
            ApiResponse<ParkingLotClient.PolicyDto> policyResponse =
                    parkingLotClient.getPolicyByLotIdAndType(reservation.getParkingLotId(), "EARLY_CANCEL_REFUND_BEFORE");

            if (policyResponse != null && policyResponse.data() != null) {
                refundWindowMinutes = policyResponse.data().value();
                log.info("Got refund policy for lot {}: {} minutes before reservation",
                        reservation.getParkingLotId(), refundWindowMinutes);
            }
        } catch (Exception e) {
            log.warn("Failed to get refund policy for parking lot {}. Using default.",
                    reservation.getParkingLotId(), e);
        }

        if (refundWindowMinutes == null) {
            refundWindowMinutes = 30;
            log.info("Using default refund window: 30 minutes");
        }

        // Calculate refund deadline (reservedFrom - refundWindowMinutes)
        LocalDateTime refundDeadline = reservation.getReservedFrom().minusMinutes(refundWindowMinutes);

        boolean isEligible = now.isBefore(refundDeadline);
        log.info("Refund eligibility check for reservation {}: now={}, refundDeadline={}, eligible={}",
                reservation.getId(), now, refundDeadline, isEligible);

        return isEligible;
    }

    /**
     * Process refund for cancelled reservation
     */
    private void processRefund(Reservation reservation) {
        if (reservation.getInitialFee() == null || reservation.getInitialFee().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No refund needed for reservation {} (initial fee is zero or null)", reservation.getId());
            return;
        }

        processRefundTransaction(reservation, reservation.getInitialFee(), "Hoàn tiền hủy đặt chỗ: " + reservation.getId());
    }

    @Override
    public Page<ReservationResponse> getReservations(int page, int size, String sortBy, String sortOrder, ReservationSearchCriteria criteria, String userIdHeader) {
        Long userId = null;
        if (criteria.getOwnedByMe() == null) {
            criteria.setOwnedByMe(false);
        }
        if (userIdHeader != null && criteria.getOwnedByMe() == true) {
            try {
                userId = Long.parseLong(userIdHeader);
                log.info("Parsed user ID from header: {}", userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }

        // Only filter by userId if ownedByMe is true or not specified
        if (userId != null && (Boolean.TRUE.equals(criteria.getOwnedByMe()) || criteria.getOwnedByMe() == null)) {
            log.info("Filtering reservations for user ID: {}", userId);
        }

        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        Predicate predicate = ReservationSpecification.buildPredicate(criteria, userId);

        Page<Reservation> reservations = reservationRepository.findAll(predicate, pageable);

        return reservations.map(this::getReservationResponse);
    }

    @Override
    public List<SyncReservationResponse> getReservationForSyncing(Long lotId, ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findAllByParkingLotIdAndStatus(lotId, status);
        return reservations.stream()
                .map(reservation -> reservationMapper.toSyncResponse(reservation, userService, vehicleService))
                .toList();
    }

    @Override
    @Transactional
    public void updateReservation(Long id, SyncReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found: " + id));

        ReservationStatus newStatus = request.getStatus();
        reservation.setStatus(newStatus);
        reservation.setSessionId(request.getSessionId());
        reservation.setTotalFee(request.getTotalFee());
        reservation.setReservedUntil(request.getReservedUntil());
        reservation = reservationRepository.save(reservation);

        if (newStatus == ReservationStatus.COMPLETED) {
            deductWalletAfterCompletingReservation(reservation);
        } else if (newStatus == ReservationStatus.CANCELLED) {
            logCancelledReservation(reservation);
        }

        sendNotificationForStatus(reservation.getId(), newStatus);
    }

    @Override
    public Long checkOverlap(Long parkingLotId, LocalDateTime start, Integer assumedStayMinute, VehicleType vehicleType) {
        LocalDateTime end = start.plusMinutes(assumedStayMinute);

        log.debug("Checking overlap for parking lot: {}, time window: {} to {}, vehicle type: {}",
                parkingLotId, start, end, vehicleType.name());

        Long count = reservationRepository.findOverlapReservations(
                parkingLotId, start, end, vehicleType.name());

        log.debug("Found {} overlapping reservations", count);

        return count != null ? count : 0L;
    }

    @NonNull
    private ReservationResponse getReservationResponse(Reservation reservation) {
        ReservationResponse response = reservationMapper.toResponse(reservation, parkingLotClient, vehicleService);
        String qrCodeContent = generateQRCodeContent(reservation);
        String qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64(qrCodeContent);
        response.setQrCode(qrCodeBase64);

        // For ACTIVE reservations, calculate and set estimated totalFee
        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            BigDecimal estimatedTotalFee = calculateCurrentTotalAmount(reservation);
            response.setTotalFee(estimatedTotalFee);
            log.debug("Set estimated totalFee for ACTIVE reservation {}: {} VND",
                    reservation.getId(), estimatedTotalFee);
        }

        return response;
    }

    private BigDecimal calculateCurrentTotalAmount(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now().plusHours(7);

        // Fetch pricing rule details from parking-lot-service
        ParkingLotClient.PricingRuleDetailsDto pricingRule = null;
        try {
            ApiResponse<ParkingLotClient.PricingRuleDetailsDto> response =
                    parkingLotClient.getPricingRuleById(reservation.getPricingRuleId());
            if (response != null && response.success() && response.data() != null) {
                pricingRule = response.data();
            }
        } catch (Exception e) {
            log.error("Failed to fetch pricing rule {} for reservation {}",
                    reservation.getPricingRuleId(), reservation.getId(), e);
            return reservation.getInitialFee(); // Fallback to initial fee
        }

        if (pricingRule == null) {
            log.warn("Pricing rule not found for reservation {}, using initial fee", reservation.getId());
            return reservation.getInitialFee();
        }

        // Calculate duration from entry time (reservedFrom) to now
        long durationMinutes = ChronoUnit.MINUTES.between(reservation.getReservedFrom(), now);

        // Calculate remaining minutes after initial duration
        long remainingMinutes = durationMinutes - pricingRule.initialDurationMinute();

        BigDecimal estimatedTotal;

        if (remainingMinutes > 0) {
            // Calculate additional blocks
            long blocks = (long) Math.ceil((double) remainingMinutes / pricingRule.stepMinute());

            BigDecimal initialCharge = BigDecimal.valueOf(pricingRule.initialCharge());
            BigDecimal additionalCharge = BigDecimal.valueOf(blocks * pricingRule.stepRate());
            estimatedTotal = initialCharge.add(additionalCharge);
        } else {
            // Still within initial duration
            estimatedTotal = BigDecimal.valueOf(pricingRule.initialCharge());
        }

        log.debug("Estimated total for reservation {}: {} VND (duration: {} minutes)",
                reservation.getId(), estimatedTotal, durationMinutes);

        return estimatedTotal;
    }

    private void sendNotificationForStatus(Long reservationId, ReservationStatus status) {
        log.info("🔔 [SEND-NOTIFICATION] Called for reservation: {}, status: {}", reservationId, status);
        try {
            switch (status) {
                case PENDING:
                    log.info("🔔 [SEND-NOTIFICATION] Routing to sendNewReservationNotification");
                    sendNewReservationNotification(reservationId);
                    break;
                case ACTIVE:
                    log.info("🔔 [SEND-NOTIFICATION] Routing to sendActiveReservationNotification");
                    sendActiveReservationNotification(reservationId);
                    break;
                case COMPLETED:
                    log.info("🔔 [SEND-NOTIFICATION] Routing to sendCompletedReservationNotification");
                    sendCompletedReservationNotification(reservationId);
                    break;
                case CANCELLED:
                    log.info("🔔 [SEND-NOTIFICATION] Routing to sendCancelledReservationNotification");
                    sendCancelledReservationNotification(reservationId);
                    break;
                default:
                    log.debug("No notification configured for status: {}", status);
            }
            log.info("✅ [SEND-NOTIFICATION] Completed for reservation: {}, status: {}", reservationId, status);
        } catch (Exception e) {
            log.error("❌ [SEND-NOTIFICATION] Error for reservation: {}, status: {}", reservationId, status, e);
            throw e;
        }
    }

    private void sendNewReservationNotification(Long reservationId) {
        log.info("🔔 Starting PENDING notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_CREATED,
                "ĐẶT CHỖ THÀNH CÔNG",
                reservation -> {
                    String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, reservation.getParkingLotId());
                    String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";
                    String time = reservation.getReservedFrom().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm"));
                    String message = String.format("Bạn đã đặt chỗ thành công cho xe có biển số %s tại bãi xe %s vào %s",
                            reservation.getVehicle().getLicensePlate(),
                            lotName,
                            time);
                    log.info("🔔 Notification message built: {}", message);
                    return message;
                }
        );
    }

    private void sendActiveReservationNotification(Long reservationId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting ACTIVE notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_ACTIVATED,
                "XE CỦA BẠN ĐÃ VÀO BÃI",
                reservation -> {
                    long feignStartTime = System.currentTimeMillis();
                    String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, reservation.getParkingLotId());
                    long feignEndTime = System.currentTimeMillis();

                    log.info("Feign call to get parking lot name took: {}ms", (feignEndTime - feignStartTime));

                    String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";
                    String time = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm"));

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("ACTIVE notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format("Xe của bạn đã vào bãi xe %s vào %s", lotName, time);
                }
        );
    }

    private void sendCompletedReservationNotification(Long reservationId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting COMPLETED notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_COMPLETED,
                "XE CỦA BẠN ĐÃ RA KHỎI BÃI",
                reservation -> {
                    String time = reservation.getReservedUntil() != null
                            ? reservation.getReservedUntil().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm"))
                            : "không xác định";

                    String totalFee = (reservation.getTotalFee() != null)
                            ? String.format("%,.0f", reservation.getTotalFee())
                            : "0";

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("COMPLETED notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format(
                            "Lượt đặt chỗ của bạn đã hoàn thành vào %s. Tổng số phí phải trả: %s VND. Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của Parkmate!",
                            time, totalFee
                    );
                }
        );
    }

    private void sendCancelledReservationNotification(Long reservationId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting CANCELLED notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_CANCELLED,
                "ĐẶT CHỖ ĐÃ BỊ HỦY",
                reservation -> {
                    String refundInfo;
                    if (reservation.getInitialFee() != null && reservation.getInitialFee().compareTo(BigDecimal.ZERO) > 0) {
                        String refundAmount = String.format("%,.0f", reservation.getInitialFee());
                        refundInfo = String.format("Số tiền %s VND đã được hoàn lại vào ví của bạn.", refundAmount);
                    } else {
                        refundInfo = "Không có khoản tiền nào được hoàn lại.";
                    }

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("CANCELLED notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format(
                            "Đặt chỗ của bạn đã bị hủy. %s",
                            refundInfo
                    );
                }
        );
    }


    private void sendReservationNotificationWithContent(
            Long reservationId,
            NotificationEventType eventType,
            String title,
            java.util.function.Function<Reservation, String> messageBuilder) {
        try {
            log.info("🔔 [NOTIFICATION] Starting notification process for reservation: {}, eventType: {}",
                    reservationId, eventType.getValue());

            // 1. Get reservation info
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found: " + reservationId));
            log.info("🔔 [NOTIFICATION] Found reservation: {}", reservation.getId());

            // 2. Get user info
            User user = userRepository.findById(reservation.getUser().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found: " + reservation.getUser().getId()));
            log.info("🔔 [NOTIFICATION] Found user: {} (ID: {})", user.getFullName(), user.getId());

            // 3. Get all FCM tokens for the user
            List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());
            log.info("🔔 [NOTIFICATION] Found {} device tokens for user {}", deviceTokens.size(), user.getId());

            if (deviceTokens.isEmpty()) {
                log.warn("⚠️ [NOTIFICATION] No active device tokens found for user: {} - notification type: {}",
                        user.getId(), eventType.getValue());
                return;
            }

            // 4. Build common notification data
            Map<String, Object> notificationData = buildReservationNotificationData(reservation);

            String dataJson;
            try {
                dataJson = objectMapper.writeValueAsString(notificationData);
            } catch (Exception e) {
                log.error("Failed to serialize notification data", e);
                dataJson = null;
            }

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType.getValue())
                    .recipientId(user.getAccount().getId())
                    .recipientEmail(user.getAccount().getEmail())
                    .title(title)
                    .message(messageBuilder.apply(reservation))
                    .notificationType("PUSH")
                    .deviceTokens(deviceTokens)
                    .data(dataJson)
                    .createdAt(LocalDateTime.now())
                    .sourceService("user-service")
                    .build();

            log.info("🔔 [NOTIFICATION] Built notification event: eventId={}, eventType={}, recipientId={}, title={}, deviceTokens={}",
                    event.getEventId(), event.getEventType(), event.getRecipientId(), event.getTitle(), deviceTokens.size());

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event);

            log.info("✅ [NOTIFICATION] {} notification published to Kafka topic '{}' for reservation: {} to {} devices",
                    eventType.getValue(), KafkaTopics.NOTIFICATION.getTopicName(), reservationId, deviceTokens.size());
        } catch (Exception e) {
            log.error("❌ [NOTIFICATION] Failed to publish {} notification for reservation: {}",
                    eventType.getValue(), reservationId, e);
        }
    }

    private Map<String, Object> buildReservationNotificationData(Reservation reservation) {
        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("parkingLotId", reservation.getParkingLotId());
        data.put("reservedFrom", reservation.getReservedFrom().toString());

        // reservedUntil is null for PENDING status
        if (reservation.getReservedUntil() != null) {
            data.put("reservedUntil", reservation.getReservedUntil().toString());
        }

        data.put("initialFee", reservation.getInitialFee().toString());

        if (reservation.getTotalFee() != null) {
            data.put("totalFee", reservation.getTotalFee().toString());
        }

        data.put("status", reservation.getStatus().name());

        return data;
    }

    /**
     * Get partner ID for a parking lot.
     * Returns null if partner ID cannot be retrieved.
     */
    private Long getPartnerIdForParkingLot(Long parkingLotId) {
        try {
            ApiResponse<ParkingLotClient.PartnerIdDto> partnerResponse =
                    parkingLotClient.getPartnerIdByParkingLotId(parkingLotId);
            if (partnerResponse != null && partnerResponse.data() != null) {
                Long partnerId = partnerResponse.data().partnerId();
                log.debug("Retrieved partner ID {} for parking lot {}", partnerId, parkingLotId);
                return partnerId;
            }
        } catch (Exception e) {
            log.warn("Failed to get partner ID for parking lot {}. Proceeding without partner.", parkingLotId, e);
        }
        return null;
    }

    /**
     * Validate payment/refund response from payment service.
     * Throws AppException if validation fails.
     */
    private void validateTransactionResponse(
            ResponseEntity<ApiResponse<WalletTransactionResponse>> response,
            Long reservationId,
            String operationType) {

        if (!response.hasBody() || response.getBody() == null) {
            log.error("Payment service returned empty response for {} of reservation {}", operationType, reservationId);
            throw new AppException(
                    ErrorCode.WALLET_TRANSACTION_NOT_FOUND,
                    String.format("%s service is unavailable", operationType)
            );
        }

        ApiResponse<WalletTransactionResponse> apiResponse = response.getBody();

        if (!apiResponse.success()) {
            log.error("{} failed for reservation {}: {}", operationType, reservationId, apiResponse.message());
            throw new AppException(ErrorCode.WALLET_TRANSACTION_NOT_FOUND, apiResponse.message());
        }

        WalletTransactionResponse txn = apiResponse.data();
        if (txn == null || !TransactionStatus.COMPLETED.toString().equals(txn.getStatus())) {
            log.error("{} transaction not completed for reservation {}: status={}",
                    operationType, reservationId, txn != null ? txn.getStatus() : "null");
            throw new AppException(
                    ErrorCode.WALLET_TRANSACTION_NOT_FOUND,
                    String.format("%s transaction not completed", operationType)
            );
        }
    }

    /**
     * Process refund transaction.
     * Returns true if refund was successful, false otherwise.
     */
    private void processRefundTransaction(Reservation reservation, BigDecimal amount, String description) {
        Long partnerId = getPartnerIdForParkingLot(reservation.getParkingLotId());

        try {
            ResponseEntity<ApiResponse<WalletTransactionResponse>> refundResponse = paymentClient.refundWallet(
                    CreateTransactionRequest.builder()
                            .userId(reservation.getUser().getId())
                            .partnerId(partnerId)
                            .amount(amount)
                            .transactionType(TransactionConstants.TYPE_REFUND)
                            .processedAt(LocalDateTime.now())
                            .description(description)
                            .build()
            );

            validateTransactionResponse(refundResponse, reservation.getId(), "Refund");
            log.info("✅ Successfully refunded {} VND for reservation {}", amount, reservation.getId());

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error processing refund for reservation {}", reservation.getId(), e);
            throw new AppException(ErrorCode.WALLET_TRANSACTION_NOT_FOUND, "Refund processing failed: " + e.getMessage());
        }
    }

    /**
     * Process wallet deduction transaction.
     * Returns true if deduction was successful, false otherwise.
     */
    private void processWalletDeduction(Reservation reservation, BigDecimal amount, String description) {
        Long partnerId = getPartnerIdForParkingLot(reservation.getParkingLotId());

        try {
            ResponseEntity<ApiResponse<WalletTransactionResponse>> response = paymentClient.deductWallet(
                    CreateTransactionRequest.builder()
                            .userId(reservation.getUser().getId())
                            .partnerId(partnerId)
                            .amount(amount)
                            .transactionType(TransactionConstants.TYPE_RESERVATION)
                            .processedAt(LocalDateTime.now())
                            .description(description)
                            .build()
            );

            validateTransactionResponse(response, reservation.getId(), "Deduction");
            log.info("✅ Successfully deducted {} VND for reservation {}", amount, reservation.getId());

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error deducting wallet for reservation {}", reservation.getId(), e);
            throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment processing failed: " + e.getMessage());
        }
    }

    private void logCancelledReservation(Reservation reservation) {
        BigDecimal initialFee = reservation.getInitialFee() != null ? reservation.getInitialFee() : BigDecimal.ZERO;
        log.info("Reservation {} cancelled. Initial fee: {} VND. Processing refund...",
                reservation.getId(), initialFee);

        if (initialFee.compareTo(BigDecimal.ZERO) > 0) {
            try {
                processRefundTransaction(
                        reservation,
                        initialFee,
                        String.format("Refund for cancelled reservation %d", reservation.getId())
                );
            } catch (Exception e) {
                log.error("❌ Error processing refund for cancelled reservation {}", reservation.getId(), e);
            }
        } else {
            log.info("No refund needed for reservation {} (initial fee is zero)", reservation.getId());
        }
    }

    private void deductWalletAfterCompletingReservation(Reservation reservation) {
        if (reservation.getTotalFee() == null) {
            log.error("Total fee is null for reservation: {}", reservation.getId());
            return;
        }

        if (reservation.getInitialFee() == null) {
            log.error("Initial fee is null for reservation: {}", reservation.getId());
            return;
        }

        BigDecimal deductionAmount = reservation.getTotalFee().subtract(reservation.getInitialFee());

        log.info("Deduction calculation for reservation {}: totalFee={}, initialFee={}, deduction={}",
                reservation.getId(), reservation.getTotalFee(), reservation.getInitialFee(), deductionAmount);

        // Case 1: User needs to pay more (totalFee > initialFee)
        if (deductionAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                processWalletDeduction(
                        reservation,
                        deductionAmount,
                        String.format("Additional charge for reservation %d (Total: %s VND - Prepaid: %s VND)",
                                reservation.getId(), reservation.getTotalFee(), reservation.getInitialFee())
                );
            } catch (Exception e) {
                log.error("Error deducting additional charge for reservation {}", reservation.getId(), e);
            }
        }
        // Case 2: User paid more than actual cost (totalFee < initialFee) - refund overpayment
        else if (deductionAmount.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal refundAmount = deductionAmount.abs();
            log.info("User overpaid for reservation {}. Refund amount: {} VND. Processing refund...",
                    reservation.getId(), refundAmount);

            try {
                processRefundTransaction(
                        reservation,
                        refundAmount,
                        String.format("Refund for overpayment on reservation %d (Paid: %s VND, Actual: %s VND)",
                                reservation.getId(), reservation.getInitialFee(), reservation.getTotalFee())
                );
            } catch (Exception e) {
                log.error("❌ Error processing overpayment refund for reservation {}", reservation.getId(), e);
            }
        }
        // Case 3: Exact match (totalFee == initialFee)
        else {
            log.info("No additional charge needed for reservation {} (exact match)", reservation.getId());
        }
    }


}

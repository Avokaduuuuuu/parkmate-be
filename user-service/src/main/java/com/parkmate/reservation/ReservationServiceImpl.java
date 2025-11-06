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
import com.parkmate.userSubscription.UserSubscriptionRepository;
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
import java.time.ZonedDateTime;
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
    private final UserSubscriptionRepository userSubscriptionRepository;
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
        if (data instanceof ParkingLotClient.ParkingLotNameDto parkingLot) {
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
            ResponseEntity<ApiResponse<WalletTransactionResponse>> paymentResult = paymentClient.deductWallet(
                    CreateTransactionRequest.builder()
                            .userId(reservation.getUser().getId())
                            .amount(reservation.getInitialFee())
                            .transactionType(TransactionConstants.TYPE_DEDUCTION)
                            .processedAt(LocalDateTime.now())
                            .description("Cọc đặt chỗ: " + reservation.getId())
                            .build()
            );

            if (!paymentResult.hasBody() || paymentResult.getBody() == null) {
                log.error("Payment service returned empty response for reservation ID: {}", reservation.getId());
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment service is unavailable");
            }

            ApiResponse<WalletTransactionResponse> paymentResponse = paymentResult.getBody();

            if (!paymentResponse.success()) {
                log.warn("Payment failed for reservation ID: {}. Reason: {}",
                        reservation.getId(), paymentResponse.message());
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                // Return the specific error message from payment service (e.g., "Insufficient balance")
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, paymentResponse.message());
            }

            log.info("Payment successful for reservation ID: {}, transaction ID: {}",
                    reservation.getId(),
                    paymentResponse.data() != null ? paymentResponse.data().getSessionId() : "N/A");

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
        sendNotificationForStatus(reservation.getId(), ReservationStatus.PENDING);
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
    public void cancelReservation(Long id) {

    }

    @Override
    public Page<ReservationResponse> getReservations(int page, int size, String sortBy, String sortOrder, ReservationSearchCriteria criteria, String userIdHeader) {
        Long userId = null;
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
        return response;
    }

    private void sendNotificationForStatus(Long reservationId, ReservationStatus status) {
        switch (status) {
            case PENDING:
                sendNewReservationNotification(reservationId);
                break;
            case ACTIVE:
                sendActiveReservationNotification(reservationId);
                break;
            case COMPLETED:
                sendCompletedReservationNotification(reservationId);
                break;
            case CANCELLED:
                sendCancelledReservationNotification(reservationId);
                break;
            default:
                log.debug("No notification configured for status: {}", status);
        }
    }

    private void sendNewReservationNotification(Long reservationId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting PENDING notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_CREATED,
                "ĐẶT CHỖ THÀNH CÔNG",
                reservation -> {
                    String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, reservation.getParkingLotId());
                    String lotName = (parkingLotName != null) ? parkingLotName : "the parking lot";
                    String time = ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
                    return String.format("BẠN ĐÃ ĐẶT CHỖ THÀNH CÔNG CHO XE CÓ BIỂN SỐ %s TẠI BÃI XE %s VÀO LÚC %s",
                            reservation.getVehicle().getLicensePlate(),
                            lotName,
                            time);
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

                    String lotName = (parkingLotName != null) ? parkingLotName : "the parking lot";
                    String time = ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("ACTIVE notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format("Xe của bạn đã vào bãi xe %s vào lúc %s", lotName, time);
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
                    String time = reservation.getReservedUntil().toString();
                    String totalFee = (reservation.getTotalFee() != null) ? reservation.getTotalFee().toString() : "0";

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("COMPLETED notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format(
                            """
                                    Lượt đặt chỗ tại của bạn đã hoàn thành vào lúc %s.\s
                                     Tổng số phí phải trả: %s VND\s
                                    Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của Parkmate""",
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
                "Reservation Cancelled",
                reservation -> {
                    String refundAmount = (reservation.getInitialFee() != null) ? reservation.getInitialFee().toString() : "0";

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("CANCELLED notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format(
                            "Your reservation has been cancelled. Refund of %s VND will be processed to your wallet.",
                            refundAmount
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
            // 1. Get reservation info
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found: " + reservationId));

            // 2. Get user info
            User user = userRepository.findById(reservation.getUser().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found: " + reservation.getUser().getId()));

            // 3. Get all FCM tokens for the user
            List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

            if (deviceTokens.isEmpty()) {
                log.warn("No active device tokens found for user: {} - notification type: {}",
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

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event);

            log.info("{} notification published for reservation: {} to {} devices",
                    eventType.getValue(), reservationId, deviceTokens.size());
        } catch (Exception e) {
            log.error("Failed to publish {} notification for reservation: {}",
                    eventType.getValue(), reservationId, e);
        }
    }

    private Map<String, Object> buildReservationNotificationData(Reservation reservation) {
        Map<String, Object> data = new HashMap<>();
        data.put("reservationId", reservation.getId());
        data.put("parkingLotId", reservation.getParkingLotId());
        data.put("reservedFrom", reservation.getReservedFrom().toString());
        data.put("reservedUntil", reservation.getReservedUntil().toString());
        data.put("initialFee", reservation.getInitialFee().toString());

        if (reservation.getTotalFee() != null) {
            data.put("totalFee", reservation.getTotalFee().toString());
        }

        data.put("status", reservation.getStatus().name());

        return data;
    }

    private void logCancelledReservation(Reservation reservation) {
        log.info("Reservation {} cancelled. Initial fee: {} VND (TODO: implement refund policy)",
                reservation.getId(),
                reservation.getInitialFee() != null ? reservation.getInitialFee() : BigDecimal.ZERO);
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

        if (deductionAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                ResponseEntity<ApiResponse<WalletTransactionResponse>> response = paymentClient.deductWallet(
                        CreateTransactionRequest.builder()
                                .userId(reservation.getUser().getId())
                                .amount(deductionAmount)
                                .transactionType(TransactionConstants.TYPE_DEDUCTION)
                                .description(String.format("Additional charge for reservation %d (Total: %s VND - Prepaid: %s VND)",
                                        reservation.getId(), reservation.getTotalFee(), reservation.getInitialFee()))
                                .build()
                );

                // Validate response
                if (!response.hasBody() || response.getBody() == null) {
                    log.error("Payment service returned empty response for reservation {}", reservation.getId());
                    return;
                }

                ApiResponse<WalletTransactionResponse> paymentResponse = response.getBody();

                if (!paymentResponse.success()) {
                    log.error("Failed to deduct wallet for reservation {}: {}",
                            reservation.getId(), paymentResponse.message());
                    return;
                }

                WalletTransactionResponse txn = paymentResponse.data();
                if (txn == null || !TransactionStatus.COMPLETED.toString().equals(txn.getStatus())) {
                    log.error("Transaction not completed for reservation {}: status={}",
                            reservation.getId(), txn != null ? txn.getStatus() : "null");
                    return;
                }

                log.info("Successfully deducted {} VND for reservation {}", deductionAmount, reservation.getId());

            } catch (Exception e) {
                log.error("Unexpected error deducting wallet for reservation {}", reservation.getId(), e);
            }
        }
        // Case 2: User paid more than actual cost (totalFee < initialFee) - TODO: implement refund policy
        else if (deductionAmount.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal refundAmount = deductionAmount.abs();
            log.info("User overpaid for reservation {}. Refund amount: {} VND (TODO: implement refund policy)",
                    reservation.getId(), refundAmount);
        }
        // Case 3: Exact match (totalFee == initialFee)
        else {
            log.info("No additional charge needed for reservation {} (exact match)", reservation.getId());
        }
    }


}

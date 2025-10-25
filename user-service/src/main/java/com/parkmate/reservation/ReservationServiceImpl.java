package com.parkmate.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.constants.TransactionConstants;
import com.parkmate.client.dto.request.CreateTransactionRequest;
import com.parkmate.client.dto.response.WalletTransactionResponse;
import com.parkmate.client.enums.TransactionStatus;
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
import com.parkmate.vehicle.VehicleService;
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
import java.time.format.DateTimeFormatter;
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

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, String userId) {

        if (userId != null && request.isOwnedByMe()) {
            long userIdLong = Long.parseLong(userId);
            User user = userRepository.findByAccountId(userIdLong)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            request.setUserId(user.getId());
        }

        if (request.getUserId() == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        if (request.getReservedFrom().isAfter(request.getReservedUntil())) {
            throw new AppException(ErrorCode.INVALID_RESERVATION_TIME, "From must be < to");
        }

        // Create a reservation with PENDING_PAYMENT status
        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .spotId(request.getSpotId())
                .reservedFrom(request.getReservedFrom())
                .reservedUntil(request.getReservedUntil())
                .initialFee(request.getReservationFee())
                .vehicleId(request.getVehicleId())
                .parkingLotId(request.getParkingLotId())
                .status(ReservationStatus.PENDING)
                .build();

        reservation = reservationRepository.save(reservation);

        // Deduct wallet balance
        try {
            ResponseEntity<ApiResponse<WalletTransactionResponse>> paymentResult = paymentClient.deductWallet(
                    CreateTransactionRequest.builder()
                            .userId(request.getUserId())
                            .amount(request.getReservationFee())
                            .transactionType(TransactionConstants.TYPE_DEDUCTION)
                            .referenceId(reservation.getId().toString())
                            .reservationId(reservation.getId())
                            .processedAt(LocalDateTime.now())
                            .description("Reservation fee for spot ID: " + request.getSpotId())
                            .build()
            );

            // Check if payment service returned a response
            if (!paymentResult.hasBody() || paymentResult.getBody() == null) {
                log.error("Payment service returned empty response for reservation ID: {}", reservation.getId());
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment service is unavailable");
            }

            ApiResponse<WalletTransactionResponse> paymentResponse = paymentResult.getBody();

            // Check if payment was successful
            if (!paymentResponse.success()) {
                log.warn("Payment failed for reservation ID: {}. Reason: {}",
                        reservation.getId(), paymentResponse.message());
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                // Return the specific error message from payment service (e.g., "Insufficient balance")
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, paymentResponse.message());
            }

            // Payment successful
            log.info("Payment successful for reservation ID: {}, transaction ID: {}",
                    reservation.getId(),
                    paymentResponse.data() != null ? paymentResponse.data().getSessionId() : "N/A");

            // Update reservation status to CONFIRMED
            reservation.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(reservation);

        } catch (AppException e) {
            // Re-throw AppException to preserve the specific error message
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment for reservation ID: {}", reservation.getId(), e);
            // Update reservation status to CANCELLED
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment processing failed: " + e.getMessage());
        }

        // Generate QR code with reservation information
        return getReservationResponse(reservation);
    }

    /**
     * Generate QR code content as JSON string
     */
    private String generateQRCodeContent(Reservation reservation) {
        try {
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("reservationId", reservation.getId());
            qrData.put("userId", reservation.getUserId());
            qrData.put("vehicleId", reservation.getVehicleId());
            qrData.put("parkingLotId", reservation.getParkingLotId());
            qrData.put("spotId", reservation.getSpotId());
            qrData.put("initialFee", reservation.getInitialFee());
            qrData.put("reservedFrom", reservation.getReservedFrom().toString());
            qrData.put("status", reservation.getStatus().name());
            qrData.put("createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null);

            return objectMapper.writeValueAsString(qrData);
        } catch (Exception e) {
            log.error("Error generating QR code content for reservation ID: {}", reservation.getId(), e);
            // Fallback to simple format
            return String.format("RESERVATION:%d|USER:%d|SPOT:%d|LOT:%d",
                    reservation.getId(),
                    reservation.getUserId(),
                    reservation.getSpotId(),
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
        // Parse accountId from header and convert to User ID if needed
        Long accountId = null;
        if (userIdHeader != null) {
            try {
                accountId = Long.parseLong(userIdHeader);
                log.info("Parsed account ID from header: {}", accountId);
            } catch (NumberFormatException e) {
                log.warn("Invalid account ID in header: {}", userIdHeader);
            }
        }

        // If ownedByMe is true, convert accountId to userId
        Long userId = null;
        if (accountId != null && Boolean.TRUE.equals(criteria.getOwnedByMe())) {
            User user = userRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            userId = user.getId();
            log.info("Converted account ID {} to user ID {}", accountId, userId);
        } else if (accountId != null && criteria.getOwnedByMe() == null) {
            // Default behavior: if no ownedByMe flag, treat it as user's own reservations
            User user = userRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            userId = user.getId();
            log.info("Default: Converted account ID {} to user ID {}", accountId, userId);
        }

        // Create pageable
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);

        // Build predicate from criteria and userId
        com.querydsl.core.types.Predicate predicate = ReservationSpecification.buildPredicate(criteria, userId);

        // Query with predicate
        Page<Reservation> reservations = reservationRepository.findAll(predicate, pageable);

        // Map to response with QR code
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

        // Save reservation first
        reservation = reservationRepository.save(reservation);

        // Handle payment/refund logic within transaction
        if (newStatus == ReservationStatus.COMPLETED) {
            deductWalletAfterCompletingReservation(reservation);
        } else if (newStatus == ReservationStatus.CANCELLED) {
            logCancelledReservation(reservation);
        }

        // Send notifications after transaction commits (notifications are async via Kafka anyway)
        sendNotificationForStatus(reservation.getId(), newStatus);
    }

    @NonNull
    private ReservationResponse getReservationResponse(Reservation reservation) {
        ReservationResponse response = reservationMapper.toResponse(reservation, parkingLotClient);
        String qrCodeContent = generateQRCodeContent(reservation);
        String qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64(qrCodeContent);
        response.setQrCode(qrCodeBase64);
        return response;
    }

    private void sendNotificationForStatus(Long reservationId, ReservationStatus status) {
        switch (status) {
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

    private void sendActiveReservationNotification(Long reservationId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting ACTIVE notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_CREATED,
                "Vehicle Entered Parking Lot",
                reservation -> {
                    long feignStartTime = System.currentTimeMillis();
                    String parkingLotName = reservationMapper.getParkingLotName(parkingLotClient, reservation.getParkingLotId());
                    long feignEndTime = System.currentTimeMillis();

                    log.info("Feign call to get parking lot name took: {}ms", (feignEndTime - feignStartTime));

                    String lotName = (parkingLotName != null) ? parkingLotName : "the parking lot";
                    String time = ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("ACTIVE notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format("Your vehicle has entered %s at %s", lotName, time);
                }
        );
    }

    private void sendCompletedReservationNotification(Long reservationId) {
        long startTime = System.currentTimeMillis();
        log.info("Starting COMPLETED notification for reservation: {}", reservationId);

        sendReservationNotificationWithContent(
                reservationId,
                NotificationEventType.RESERVATION_COMPLETED,
                "Reservation Completed",
                reservation -> {
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
                    String totalFee = (reservation.getTotalFee() != null) ? reservation.getTotalFee().toString() : "0";

                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("COMPLETED notification completed for reservation: {} in {}ms", reservationId, totalTime);

                    return String.format(
                            "Reservation completed and your vehicle has exited at %s. Total fee charged: %s VND",
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
            User user = userRepository.findById(reservation.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found: " + reservation.getUserId()));

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
        data.put("spotId", reservation.getSpotId());
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
        // Validate inputs
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

        // Case 1: Need to deduct more money (totalFee > initialFee) - HAPPY CASE
        if (deductionAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                ResponseEntity<ApiResponse<WalletTransactionResponse>> response = paymentClient.deductWallet(
                        CreateTransactionRequest.builder()
                                .userId(reservation.getUserId())
                                .amount(deductionAmount)
                                .transactionType(TransactionConstants.TYPE_DEDUCTION)
                                .referenceId(reservation.getId().toString())
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

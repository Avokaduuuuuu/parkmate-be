package com.parkmate.userSubscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.PaymentClient;
import com.parkmate.client.constants.TransactionConstants;
import com.parkmate.client.dto.request.CreateTransactionRequest;
import com.parkmate.client.dto.response.WalletTransactionResponse;
import com.parkmate.client.exception.ParkingLotServiceErrorCode;
import com.parkmate.common.dto.ApiResponse;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.common.util.QRCodeGenerator;
import com.parkmate.kafka.KafkaTopics;
import com.parkmate.kafka.event.NotificationEvent;
import com.parkmate.kafka.event.NotificationEventType;
import com.parkmate.mobileDevice.MobileDeviceRepository;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import com.parkmate.userSubscription.dto.*;
import com.parkmate.vehicle.Vehicle;
import com.parkmate.vehicle.VehicleRepository;
import com.parkmate.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotClient parkingLotClient;
    private final VehicleService vehicleService;
    private final ObjectMapper objectMapper;
    private final PaymentClient paymentClient;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Override
    public UserSubscriptionResponse create(CreateUserSubscriptionRequest request, String userIdHeader) {
        if (userIdHeader != null && request.getOwnedByMe() != null && request.getOwnedByMe()) {
            request.setUserId(Long.parseLong(userIdHeader));
        }

        if (checkDuplicateUserSubscription(request)) {
            throw new AppException(ErrorCode.USER_SUBSCRIPTION_ALREADY_USED);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        if (parkingLotClient.getParkingLotName(request.getParkingLotId()) == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.PARKING_NOT_FOUND);
        }

        // Check if vehicle requires spot assignment (only CAR requires spot)
        boolean requiresSpot = isCarVehicle(vehicle.getVehicleType());

        if (requiresSpot) {
            // For cars: spot is required
            if (request.getAssignedSpotId() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Xe ô tô bắt buộc phải chọn chỗ đậu xe");
            }
            if (parkingLotClient.getSpotName(request.getAssignedSpotId()) == null) {
                throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SPOT_NOT_FOUND);
            }
        } else {
            // For motorcycles/bikes: spot is not needed, set to null
            request.setAssignedSpotId(null);
        }

        int durationValue = 0;
        String subscriptionPackageName = null;
        BigDecimal amount = null;
        Object data = parkingLotClient.getSubscription(request.getSubscriptionPackageId()).data();
        if (data == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SUBSCRIPTION_NOT_FOUND);
        } else if (data instanceof ParkingLotClient.SubscriptionDto subscriptionDto) {
            durationValue = subscriptionDto.durationValue();
            subscriptionPackageName = subscriptionDto.name();
            amount = subscriptionDto.amount();
        }

        if (!checkVehicleUser(request.getVehicleId(), request.getUserId())) {
            throw new AppException(ErrorCode.VEHICLE_NOT_BELONG_TO_USER, "Vehicle " + vehicle.getId() + "does not belong to user id " + request.getUserId());
        }


        UserSubscription userSubscription = userSubscriptionMapper.toEntity(request);

        LocalDateTime endDate = request.getStartDate().plusDays(durationValue);

        userSubscription.setUser(user);
        userSubscription.setVehicle(vehicle);
        userSubscription.setStatus(UserSubscriptionStatus.INACTIVE);
        userSubscription.setEndDate(endDate);
        userSubscription.setSyncStatus(SyncStatus.PENDING);
        userSubscription.setPaidAmount(amount);
        userSubscription.setAutoRenew(true);

        try {
            deductSubscriptionFee(userSubscription, subscriptionPackageName, amount);
            UserSubscription savedSubscription = userSubscriptionRepository.save(userSubscription);
            // Only release spot if it was assigned (for cars)
            if (request.getAssignedSpotId() != null) {
                releaseSpotAfterSubscription(request.getAssignedSpotId(), String.valueOf(request.getUserId()));
            }
            return getUserSubscriptionResponse(savedSubscription);
        } catch (Exception e) {
            // Only release spot if it was assigned (for cars)
            if (request.getAssignedSpotId() != null) {
                releaseSpotAfterSubscription(request.getAssignedSpotId(), String.valueOf(request.getUserId()));
            }
            throw e;
        }
    }

    /**
     * Check if the vehicle type is a car (requires spot assignment)
     */
    private boolean isCarVehicle(com.parkmate.vehicle.VehicleType vehicleType) {
        return vehicleType == com.parkmate.vehicle.VehicleType.CAR_UP_TO_9_SEATS;
    }

    @Override
    public UserSubscriptionResponse findById(Long id) {

        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));

        return getUserSubscriptionResponse(userSubscription);
    }

    @Override
    public Page<UserSubscriptionResponse> findAll(int page,
                                                  int size,
                                                  String sortBy,
                                                  String sortOrder,
                                                  String userHeadId,
                                                  UserSubscriptionSearchCriteria searchCriteria) {
        if (userHeadId != null && searchCriteria.getOwnedByMe()) {
            searchCriteria.setUserId(Long.valueOf(userHeadId));
        }

        Page<UserSubscription> userSubscriptionPage = userSubscriptionRepository.findAll(
                UserSubscriptionSpecification.buildPredicate(searchCriteria),
                PaginationUtil.parsePageable(page, size, sortBy, sortOrder));

        return userSubscriptionPage.map(this::getUserSubscriptionResponse);
    }

    @Override
    public UserSubscriptionResponse update(Long id, UpdateUserSubscriptionRequest request) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));
        userSubscription.setSyncStatus(SyncStatus.PENDING);
        userSubscriptionMapper.updateEntityFromDto(request, userSubscription);
        return getUserSubscriptionResponse(userSubscriptionRepository.save(userSubscription));
    }

    @Override
    public void delete(Long id) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));
        userSubscription.setStatus(UserSubscriptionStatus.CANCELLED);
        userSubscriptionRepository.save(userSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSubscriptionSyncResponse> getUserSubscriptionSync(Long lotId) {
        return userSubscriptionRepository.findByParkingLotIdAndSyncStatus(lotId, SyncStatus.PENDING)
                .stream()
                .map(sub -> userSubscriptionMapper.toSyncDto(sub, parkingLotClient, vehicleService))
                .toList();
    }

    @Override
    public void syncUserSubscription(Long id) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));

//        // Don't allow sync for EXPIRED or CANCELLED subscriptions
//        if (userSubscription.getStatus() == UserSubscriptionStatus.EXPIRED ||
//            userSubscription.getStatus() == UserSubscriptionStatus.CANCELLED) {
//            log.warn("⚠️ [SYNC] Cannot sync subscription {} with status {}",
//                    id, userSubscription.getStatus());
//            return;
//        }

        if (userSubscription.getSyncStatus() == SyncStatus.PENDING) {
            // First sync - just mark as synced (subscription stays INACTIVE until vehicle enters)
            userSubscription.setSyncStatus(SyncStatus.SYNCED);
            userSubscriptionRepository.save(userSubscription);
            log.info("✅ [SYNC] Subscription {} marked as SYNCED", id);
        } else {
            // Toggle status: INACTIVE ↔ ACTIVE (vehicle entry/exit)
            UserSubscriptionStatus oldStatus = userSubscription.getStatus();
            UserSubscriptionStatus newStatus = oldStatus;

            if (userSubscription.getStatus() == UserSubscriptionStatus.ACTIVE) {
                // Vehicle exiting
                userSubscription.setStatus(UserSubscriptionStatus.INACTIVE);
                newStatus = UserSubscriptionStatus.INACTIVE;
            } else if (userSubscription.getStatus() == UserSubscriptionStatus.INACTIVE) {
                // Vehicle entering
                userSubscription.setStatus(UserSubscriptionStatus.ACTIVE);
                newStatus = UserSubscriptionStatus.ACTIVE;
            }
            userSubscriptionRepository.save(userSubscription);

            // Send notification when subscription status changes
            if (oldStatus != newStatus) {
                log.info("✅ [SYNC] Subscription {} status changed: {} -> {}", id, oldStatus, newStatus);
                sendSubscriptionStatusChangedNotification(userSubscription, oldStatus, newStatus);
            }
        }
    }

    private void sendSubscriptionStatusChangedNotification(UserSubscription subscription, UserSubscriptionStatus oldStatus, UserSubscriptionStatus newStatus) {
        log.info("🔔 [SUBSCRIPTION-STATUS] Sending status change notification for subscription {} ({} -> {})",
                subscription.getId(), oldStatus, newStatus);

        try {
            User user = subscription.getUser();
            List<String> deviceTokens = mobileDeviceRepository.findActivePushTokensByUserId(user.getId());

            if (deviceTokens.isEmpty()) {
                log.warn("⚠️ [SUBSCRIPTION-STATUS] No device tokens found for user {}", user.getId());
                return;
            }

            String parkingLotName = null;
            try {
                parkingLotName = parkingLotClient.getParkingLotName(subscription.getParkingLotId()).data().name();
            } catch (Exception e) {
                log.warn("⚠️ [SUBSCRIPTION-STATUS] Failed to fetch parking lot name for lot {}", subscription.getParkingLotId());
            }
            String lotName = (parkingLotName != null) ? parkingLotName : "bãi xe";

            String title;
            String message;

            if (newStatus == UserSubscriptionStatus.ACTIVE) {
                title = "XE CỦA BẠN ĐÃ VÀO BÃI";
                message = String.format(
                        "Xe biển số %s đã vào bãi %s bằng vé tháng.",
                        subscription.getVehicle().getLicensePlate(),
                        lotName
                );
            } else if (newStatus == UserSubscriptionStatus.INACTIVE) {
                title = "XE CỦA BẠN ĐÃ RA KHỎI BÃI";
                message = String.format(
                        "Xe biển số %s đã ra khỏi bãi %s. Vé tháng sẵn sàng cho lần vào tiếp theo.",
                        subscription.getVehicle().getLicensePlate(),
                        lotName
                );
            } else if (newStatus == UserSubscriptionStatus.EXPIRED) {
                title = "VÉ THÁNG ĐÃ HẾT HẠN";
                message = String.format(
                        "Vé tháng của bạn tại %s đã hết hạn.",
                        lotName
                );
            } else if (newStatus == UserSubscriptionStatus.CANCELLED) {
                title = "VÉ THÁNG ĐÃ BỊ HỦY";
                message = String.format(
                        "Vé tháng của bạn tại %s đã bị hủy. Liên hệ hỗ trợ nếu cần thiết.",
                        lotName
                );
            } else {
                log.warn("⚠️ [SUBSCRIPTION-STATUS] Unknown status {}, skipping notification", newStatus);
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("subscriptionId", subscription.getId());
            data.put("parkingLotId", subscription.getParkingLotId());
            data.put("oldStatus", oldStatus.name());
            data.put("newStatus", newStatus.name());
            data.put("deepLink", "parkmate://subscription/" + subscription.getId());

            String dataJson = objectMapper.writeValueAsString(data);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(NotificationEventType.SUBSCRIPTION_STATUS_CHANGED.getValue())
                    .recipientId(user.getAccount().getId())
                    .recipientEmail(user.getAccount().getEmail())
                    .title(title)
                    .message(message)
                    .notificationType("PUSH")
                    .deviceTokens(deviceTokens)
                    .data(dataJson)
                    .createdAt(LocalDateTime.now())
                    .sourceService("user-service")
                    .build();

            kafkaTemplate.send(
                    KafkaTopics.NOTIFICATION.getTopicName(),
                    event.getEventId(),
                    event
            );

            log.info("✅ [SUBSCRIPTION-STATUS] Notification sent for subscription {} ({} devices)", subscription.getId(), deviceTokens.size());
        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION-STATUS] Error sending notification for subscription {}", subscription.getId(), e);
        }
    }

    @Override
    public List<Long> findOccupiedSpots(List<Long> parkingLotId, LocalDateTime startTime, LocalDateTime endTime) {
        return userSubscriptionRepository.findOccupiedSpotIds(parkingLotId, startTime, endTime);
    }

    private boolean checkVehicleUser(Long vehicleId, Long userId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> vehicle.getUser().getId().equals(userId))
                .orElse(false);
    }

    private String generateQRCodeContent(UserSubscription userSubscription) {
        try {
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("userSubscriptionId", userSubscription.getId());
            qrData.put("qrType", "subscription");
            return objectMapper.writeValueAsString(qrData);
        } catch (Exception e) {
            log.error("Error generating QR code content for SUBSCRIPTION ID: {}", userSubscription.getId(), e);
            // Fallback to simple format
            return String.format("SUBSCRIPTION:%d|USER:%d|LOT:%d",
                    userSubscription.getId(),
                    userSubscription.getUser().getId(),
                    userSubscription.getParkingLotId());
        }
    }

    private void deductSubscriptionFee(UserSubscription userSubscription, String subscriptionPackageName, BigDecimal amount) {
        // Get partner ID for the parking lot
        Long partnerId = null;
        try {
            ApiResponse<ParkingLotClient.PartnerIdDto> partnerResponse =
                    parkingLotClient.getPartnerIdByParkingLotId(userSubscription.getParkingLotId());
            if (partnerResponse != null && partnerResponse.data() != null) {
                partnerId = partnerResponse.data().partnerId();
                log.info("Retrieved partner ID {} for parking lot {}", partnerId, userSubscription.getParkingLotId());
            }
        } catch (Exception e) {
            log.error("Failed to get partner ID for parking lot {}. Proceeding without partner credit.",
                    userSubscription.getParkingLotId(), e);
        }

        try {
            ResponseEntity<ApiResponse<WalletTransactionResponse>> paymentResult = paymentClient.deductWallet(
                    CreateTransactionRequest.builder()
                            .userId(userSubscription.getUser().getId())
                            .partnerId(partnerId)
                            .amount(amount)
                            .transactionType(TransactionConstants.TYPE_SUBSCRIPTION)
                            .processedAt(LocalDateTime.now())
                            .description("Thanh toán gói: " + subscriptionPackageName)
                            .build()
            );

            if (!paymentResult.hasBody() || paymentResult.getBody() == null) {
                log.error("Payment service returned empty response for subscription ID: {}", userSubscription.getId());
                userSubscription.setStatus(UserSubscriptionStatus.CANCELLED);
                userSubscriptionRepository.save(userSubscription);
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment service is unavailable");
            }

            ApiResponse<WalletTransactionResponse> paymentResponse = paymentResult.getBody();

            if (!paymentResponse.success()) {
                log.warn("Payment failed for user subscription ID: {}. Reason: {}",
                        userSubscription.getId(), paymentResponse.message());
                userSubscription.setStatus(UserSubscriptionStatus.CANCELLED);
                userSubscriptionRepository.save(userSubscription);
                // Return the specific error message from payment service (e.g., "Insufficient balance")
                throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, paymentResponse.message());
            }

            log.info("Payment successful for user subscription ID: {}, transaction ID: {}",
                    userSubscription.getId(),
                    paymentResponse.data() != null ? paymentResponse.data().getId() : "N/A");

            // Keep status as INACTIVE (vehicle is outside the lot)
            // Status will change to ACTIVE when vehicle enters via syncUserSubscription
            userSubscription.setPaidAt(LocalDateTime.now());
            userSubscriptionRepository.save(userSubscription);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment for subscription ID: {}", userSubscription.getId(), e);
            userSubscription.setStatus(UserSubscriptionStatus.CANCELLED);
            userSubscriptionRepository.save(userSubscription);
            throw new AppException(ErrorCode.WALLET_DEDUCTION_FAILED, "Payment processing failed: " + e.getMessage());
        }
//        sendNotificationForStatus(reservation.getId(), ReservationStatus.PENDING)

    }

    @NonNull
    private UserSubscriptionResponse getUserSubscriptionResponse(UserSubscription userSubscription) {
        UserSubscriptionResponse response = userSubscriptionMapper.toDto(userSubscription, parkingLotClient, vehicleService);
        String qrCodeContent = generateQRCodeContent(userSubscription);
        String qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64(qrCodeContent);
        response.setQrCode(qrCodeBase64);
        return response;
    }

    private boolean checkDuplicateUserSubscription(CreateUserSubscriptionRequest request) {
        return userSubscriptionRepository.existsByUserIdAndParkingLotIdAndVehicleIdAndStatusIn(
                request.getUserId(),
                request.getParkingLotId(),
                request.getVehicleId(),
                List.of(UserSubscriptionStatus.ACTIVE, UserSubscriptionStatus.INACTIVE)
        );
    }

    @Override
    public List<?> getFloorAvailability(Long parkingLotId, Long vehicleId, Long subscriptionPackageId, LocalDateTime startDate) {
        // Validate vehicle ownership
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        // Get subscription package to calculate endDate
        Object data = parkingLotClient.getSubscription(subscriptionPackageId).data();
        if (data == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        int durationValue = 0;
        if (data instanceof ParkingLotClient.SubscriptionDto subscriptionDto) {
            durationValue = subscriptionDto.durationValue();
        }

        LocalDateTime endDate = startDate.plusDays(durationValue);

        // Call parking-lot-service internal API
        ApiResponse<List<ParkingLotClient.FloorSubscriptionAvailabilityDto>> response =
                parkingLotClient.getFloorSubscriptionAvailability(
                        parkingLotId,
                        vehicle.getVehicleType(),
                        startDate.toString(),
                        endDate.toString()
                );

        if (!response.success() || response.data() == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, "Failed to get floor availability");
        }

        return response.data();
    }

    @Override
    public List<?> getAreaAvailability(Long floorId, Long vehicleId, Long subscriptionPackageId, LocalDateTime startDate) {
        // Validate vehicle ownership
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        // Get subscription package to calculate endDate
        Object data = parkingLotClient.getSubscription(subscriptionPackageId).data();
        if (data == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        int durationValue = 0;
        if (data instanceof ParkingLotClient.SubscriptionDto subscriptionDto) {
            durationValue = subscriptionDto.durationValue();
        }

        LocalDateTime endDate = startDate.plusDays(durationValue);

        // Call parking-lot-service internal API
        ApiResponse<List<ParkingLotClient.AreaSubscriptionAvailabilityDto>> response =
                parkingLotClient.getAreaSubscriptionAvailability(
                        floorId,
                        vehicle.getVehicleType(),
                        startDate.toString(),
                        endDate.toString()
                );

        if (!response.success() || response.data() == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, "Failed to get area availability");
        }

        return response.data();
    }

    @Override
    public List<?> getSpotAvailability(Long areaId, Long subscriptionPackageId, LocalDateTime startDate) {
        // Get subscription package to calculate endDate
        Object data = parkingLotClient.getSubscription(subscriptionPackageId).data();
        if (data == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        int durationValue = 0;
        if (data instanceof ParkingLotClient.SubscriptionDto subscriptionDto) {
            durationValue = subscriptionDto.durationValue();
        }

        LocalDateTime endDate = startDate.plusDays(durationValue);

        // Call parking-lot-service internal API
        ApiResponse<List<ParkingLotClient.SpotSubscriptionAvailabilityDto>> response =
                parkingLotClient.getSpotSubscriptionAvailability(
                        areaId,
                        startDate.toString(),
                        endDate.toString()
                );

        if (!response.success() || response.data() == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, "Failed to get spot availability");
        }

        return response.data();
    }

    @Override
    public Boolean holdSpot(Long userId, Long spotId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Call parking-lot-service to hold the spot
        ApiResponse<ParkingLotClient.SpotHoldResponseDto> response = parkingLotClient.holdSpot(spotId, String.valueOf(userId));

        if (!response.success() || response.data() == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, "Failed to hold spot");
        }

        return response.data().hasSession();
    }

    @Override
    public Object releaseSpot(Long spotId, String userIdHeader) {
        // Validate user exists
        Long userId = Long.parseLong(userIdHeader);
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Call parking-lot-service to release the spot
        ApiResponse<ParkingLotClient.SpotHoldResponseDto> response = parkingLotClient.releaseSpot(spotId, userIdHeader);

        if (!response.success()) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, response.message());
        }

        return response.data();
    }

    @Override
    public BigDecimal getTotalRevenue(Long lotId, LocalDateTime fromDate, LocalDateTime toDate) {
        return userSubscriptionRepository.getTotalRevenue(lotId, fromDate, toDate);
    }

    @Override
    public Long getTotalCount(Long lotId, LocalDateTime fromDate, LocalDateTime toDate) {
        return userSubscriptionRepository.getTotalCount(lotId, fromDate, toDate);
    }

    @Override
    @Transactional
    public UserSubscriptionResponse setRenewalDecision(Long subscriptionId, Boolean continueRenewal) {
        log.info("Setting renewal decision for subscription {}: continueRenewal={}", subscriptionId, continueRenewal);

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND));

        // Check if subscription is already expired
        if (subscription.getStatus() == UserSubscriptionStatus.EXPIRED) {
            throw new AppException(ErrorCode.USER_SUBSCRIPTION_EXPIRED,
                    "Cannot modify renewal settings for expired subscription");
        }

        // Update autoRenew flag
        subscription.setAutoRenew(continueRenewal);
        subscription.setUpdatedAt(LocalDateTime.now());

        UserSubscription updated = userSubscriptionRepository.save(subscription);

        log.info("✅ Renewal decision updated for subscription {}. autoRenew is now: {}",
                subscriptionId, continueRenewal);

        return userSubscriptionMapper.toDto(updated, parkingLotClient, vehicleService);
    }

    private void releaseSpotAfterSubscription(Long spotId, String userId) {
        try {
            log.info("Releasing spot hold for spotId: {}, userId: {}", spotId, userId);
            ApiResponse<ParkingLotClient.SpotHoldResponseDto> response = parkingLotClient.releaseSpot(spotId, userId);

            if (response.success()) {
                log.info("Successfully released spot hold for spotId: {}", spotId);
            } else {
                log.warn("Failed to release spot hold for spotId: {}. Reason: {}. Hold will auto-expire.",
                        spotId, response.message());
            }
        } catch (Exception e) {
            log.error("Error releasing spot hold for spotId: {}. Hold will auto-expire after timeout.", spotId, e);
        }
    }

    @Override
    @Transactional
    public UserSubscriptionResponse cancelSubscription(Long subscriptionId, CancelSubscriptionRequest request, String userIdHeader) {
        log.info("🔔 [SUBSCRIPTION-CANCEL] Processing cancellation for subscription {}", subscriptionId);

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, subscriptionId));

        if (userIdHeader != null) {
            Long userId = Long.parseLong(userIdHeader);
            if (!subscription.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_BELONG_TO_USER);
            }
        }

        if (subscription.getStatus() == UserSubscriptionStatus.CANCELLED) {
            throw new AppException(ErrorCode.USER_SUBSCRIPTION_ALREADY_CANCELLED);
        }

        // Check if subscription is expired
        if (subscription.getStatus() == UserSubscriptionStatus.EXPIRED) {
            throw new AppException(ErrorCode.USER_SUBSCRIPTION_EXPIRED);
        }

        // Determine refund eligibility (still allow cancellation, just no refund if not eligible)
        boolean isEligibleForRefund = checkRefundEligibility(subscription);
        BigDecimal refundAmount = BigDecimal.ZERO;

        if (isEligibleForRefund) {
            refundAmount = subscription.getPaidAmount();
            log.info("✅ [SUBSCRIPTION-CANCEL] Subscription {} is eligible for refund: {}", subscriptionId, refundAmount);

            // Process refund
            processRefund(subscription, refundAmount);
        } else {
            log.info("⚠️ [SUBSCRIPTION-CANCEL] Subscription {} is NOT eligible for refund (used or passed 1/2 period)", subscriptionId);
        }

        LocalDateTime vietnamNow = LocalDateTime.now().plusHours(7);
        UserSubscriptionStatus oldStatus = subscription.getStatus();
        subscription.setStatus(UserSubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(vietnamNow);
        subscription.setCancellationReason(request.getReason());
        subscription.setRefundAmount(refundAmount);
        subscription.setSyncStatus(SyncStatus.PENDING);

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        sendSubscriptionStatusChangedNotification(savedSubscription, oldStatus, UserSubscriptionStatus.CANCELLED);

        log.info("✅ [SUBSCRIPTION-CANCEL] Subscription {} cancelled successfully. Refund amount: {}",
                subscriptionId, refundAmount);

        return getUserSubscriptionResponse(savedSubscription);
    }

    /**
     * Check if subscription is eligible for refund
     * Not eligible if:
     * 1. Subscription has been used (has parking session)
     * 2. More than 1/2 of the subscription period has passed
     */
    private boolean checkRefundEligibility(UserSubscription subscription) {
        // Check 1: Has subscription been used?
        try {
            ApiResponse<Boolean> usageResponse = parkingLotClient.checkSubscriptionUsage(subscription.getId());
            if (usageResponse != null && usageResponse.success() && Boolean.TRUE.equals(usageResponse.data())) {
                log.info("❌ [REFUND-CHECK] Subscription {} has been used", subscription.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("⚠️ [REFUND-CHECK] Failed to check subscription usage for {}, assuming used for safety",
                    subscription.getId(), e);
            return false;
        }

        // Check 2: Has more than 1/2 of the period passed? (use Vietnam timezone UTC+7)
        LocalDateTime vietnamNow = LocalDateTime.now().plusHours(7);
        LocalDateTime startDate = subscription.getStartDate();
        LocalDateTime endDate = subscription.getEndDate();

        // If subscription hasn't started yet, eligible for refund
        if (vietnamNow.isBefore(startDate)) {
            log.info("✅ [REFUND-CHECK] Subscription {} hasn't started yet, eligible for refund", subscription.getId());
            return true;
        }

        // Calculate half point of subscription period
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        long halfDays = totalDays / 2;
        LocalDateTime halfPoint = startDate.plusDays(halfDays);

        if (vietnamNow.isAfter(halfPoint)) {
            log.info("❌ [REFUND-CHECK] Subscription {} has passed half point ({} days of {} days)",
                    subscription.getId(), java.time.temporal.ChronoUnit.DAYS.between(startDate, vietnamNow), totalDays);
            return false;
        }

        log.info("✅ [REFUND-CHECK] Subscription {} is within first half of period, eligible for refund",
                subscription.getId());
        return true;
    }

    /**
     * Process refund for cancelled subscription
     */
    private void processRefund(UserSubscription subscription, BigDecimal refundAmount) {
        // Get partner ID for the parking lot
        Long partnerId = null;
        try {
            ApiResponse<ParkingLotClient.PartnerIdDto> partnerResponse =
                    parkingLotClient.getPartnerIdByParkingLotId(subscription.getParkingLotId());
            if (partnerResponse != null && partnerResponse.data() != null) {
                partnerId = partnerResponse.data().partnerId();
            }
        } catch (Exception e) {
            log.warn("⚠️ [REFUND] Failed to get partner ID for parking lot {}", subscription.getParkingLotId());
        }

        try {
            // Use Vietnam timezone (UTC+7)
            LocalDateTime vietnamNow = LocalDateTime.now().plusHours(7);
            ResponseEntity<ApiResponse<WalletTransactionResponse>> refundResult = paymentClient.refundWallet(
                    CreateTransactionRequest.builder()
                            .userId(subscription.getUser().getId())
                            .partnerId(partnerId)
                            .amount(refundAmount)
                            .transactionType(TransactionConstants.TYPE_REFUND)
                            .processedAt(vietnamNow)
                            .description("Hoàn tiền hủy vé tháng - Subscription ID: " + subscription.getId())
                            .build()
            );

            if (refundResult.hasBody() && refundResult.getBody() != null && refundResult.getBody().success()) {
                log.info("✅ [REFUND] Successfully refunded {} for subscription {}",
                        refundAmount, subscription.getId());
            } else {
                log.error("❌ [REFUND] Failed to refund for subscription {}",
                        subscription.getId());
                throw new AppException(ErrorCode.USER_SUBSCRIPTION_CANCEL_FAILED, "Refund processing failed");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ [REFUND] Error processing refund for subscription {}", subscription.getId(), e);
            throw new AppException(ErrorCode.USER_SUBSCRIPTION_CANCEL_FAILED, "Refund processing error: " + e.getMessage());
        }
    }

}

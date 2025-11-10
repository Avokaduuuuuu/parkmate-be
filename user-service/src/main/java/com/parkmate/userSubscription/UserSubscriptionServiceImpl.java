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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (parkingLotClient.getSpotName(request.getAssignedSpotId()) == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SPOT_NOT_FOUND);
        }
        int durationValue = 0;
        String subscriptionPackageName = null;
        Object data = parkingLotClient.getSubscription(request.getSubscriptionPackageId()).data();
        if (data == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, ParkingLotServiceErrorCode.SUBSCRIPTION_NOT_FOUND);
        } else if (data instanceof ParkingLotClient.SubscriptionDto subscriptionDto) {
            durationValue = subscriptionDto.durationValue();
            subscriptionPackageName = subscriptionDto.name();
        }

        if (!checkVehicleUser(request.getVehicleId(), request.getUserId())) {
            throw new AppException(ErrorCode.VEHICLE_NOT_BELONG_TO_USER, "Vehicle " + vehicle.getId() + "does not belong to user id " + request.getUserId());
        }


        UserSubscription userSubscription = userSubscriptionMapper.toEntity(request);

        LocalDateTime endDate = request.getStartDate().plusDays(durationValue * 30L);

        userSubscription.setUser(user);
        userSubscription.setVehicle(vehicle);
        userSubscription.setStatus(UserSubscriptionStatus.ACTIVE);
        userSubscription.setEndDate(endDate);

        deductSubscriptionFee(userSubscription, subscriptionPackageName);

        UserSubscription savedSubscription = userSubscriptionRepository.save(userSubscription);
        return getUserSubscriptionResponse(savedSubscription);
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
                                                  String accountIdHeader,
                                                  UserSubscriptionSearchCriteria searchCriteria) {
        Long userHeadId = null;
        if (accountIdHeader != null && searchCriteria.getOwnedByMe()) {
            userHeadId = userRepository.getUserIdByAccountId(Long.parseLong(accountIdHeader));
        }

        Page<UserSubscription> userSubscriptionPage = userSubscriptionRepository.findAll(
                UserSubscriptionSpecification.buildPredicate(searchCriteria, userHeadId),
                PaginationUtil.parsePageable(page, size, sortBy, sortOrder));

        return userSubscriptionPage.map(this::getUserSubscriptionResponse);
    }

    @Override
    public UserSubscriptionResponse update(Long id, UpdateUserSubscriptionRequest request) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));
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
        if (userSubscription.getStatus() == UserSubscriptionStatus.ACTIVE) {
            userSubscription.setStatus(UserSubscriptionStatus.INACTIVE);
        } else if (userSubscription.getStatus() == UserSubscriptionStatus.INACTIVE) {
            userSubscription.setStatus(UserSubscriptionStatus.ACTIVE);
        }
        userSubscriptionRepository.save(userSubscription);
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

    private void deductSubscriptionFee(UserSubscription userSubscription, String subscriptionPackageName) {
        try {
            ResponseEntity<ApiResponse<WalletTransactionResponse>> paymentResult = paymentClient.deductWallet(
                    CreateTransactionRequest.builder()
                            .userId(userSubscription.getUser().getId())
                            .amount(userSubscription.getPaidAmount())
                            .transactionType(TransactionConstants.TYPE_DEDUCTION)
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
                    paymentResponse.data() != null ? paymentResponse.data().getSessionId() : "N/A");

            userSubscription.setStatus(UserSubscriptionStatus.ACTIVE);
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
        return userSubscriptionRepository.existsByUserIdAndParkingLotIdAndUserIdAndStatus(
                request.getUserId(),
                request.getParkingLotId(),
                request.getUserId(),
                UserSubscriptionStatus.ACTIVE
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

        LocalDateTime endDate = startDate.plusDays(durationValue * 30L);

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

        LocalDateTime endDate = startDate.plusDays(durationValue * 30L);

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

        LocalDateTime endDate = startDate.plusDays(durationValue * 30L);

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
        ApiResponse<Boolean> response = parkingLotClient.holdSpot(userId, spotId);

        if (!response.success() || response.data() == null) {
            throw new AppException(ErrorCode.OTHER_CLIENT_ERROR, "Failed to hold spot");
        }

        return response.data();
    }


}

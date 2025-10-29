package com.parkmate.userSubscription;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.client.exception.ParkingLotServiceErrorCode;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import com.parkmate.userSubscription.dto.CreateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UpdateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UserSubscriptionResponse;
import com.parkmate.userSubscription.dto.UserSubscriptionSearchCriteria;
import com.parkmate.vehicle.Vehicle;
import com.parkmate.vehicle.VehicleRepository;
import com.parkmate.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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

    @Override
    public UserSubscriptionResponse create(CreateUserSubscriptionRequest request, String accountIdHeader) {
        if (accountIdHeader != null) {
            request.setUserId(userRepository.getUserIdByAccountId(Long.parseLong(accountIdHeader)));
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

        if (!checkVehicleUser(request.getVehicleId(), request.getUserId())) {
            throw new AppException(ErrorCode.VEHICLE_NOT_BELONG_TO_USER, "Vehicle " + vehicle.getId() + "does not belong to user id " + request.getUserId());
        }

        UserSubscription userSubscription = userSubscriptionMapper.toEntity(request);

        userSubscription.setUser(user);
        userSubscription.setVehicle(vehicle);
        userSubscription.setStatus(UserSubscriptionStatus.ACTIVE);


        UserSubscription savedSubscription = userSubscriptionRepository.save(userSubscription);
        return userSubscriptionMapper.toDto(savedSubscription, parkingLotClient, vehicleService);
    }

    @Override
    public UserSubscriptionResponse findById(Long id) {

        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));

        return userSubscriptionMapper.toDto(userSubscription, parkingLotClient, vehicleService);
    }

    @Override
    public Page<UserSubscriptionResponse> findAll(int page,
                                                  int size,
                                                  String sortBy,
                                                  String sortOrder,
                                                  String accountIdHeader,
                                                  UserSubscriptionSearchCriteria searchCriteria) {
        Long userHeadId = null;
        if (accountIdHeader != null) {
            userHeadId = userRepository.getUserIdByAccountId(Long.parseLong(accountIdHeader));
        }

        Page<UserSubscription> userSubscriptionPage = userSubscriptionRepository.findAll(
                UserSubscriptionSpecification.buildPredicate(searchCriteria, userHeadId),
                PaginationUtil.parsePageable(page, size, sortBy, sortOrder));

        return userSubscriptionPage.map(
                userSubscription -> userSubscriptionMapper.toDto(userSubscription, parkingLotClient, vehicleService)
        );
    }

    @Override
    public UserSubscriptionResponse update(Long id, UpdateUserSubscriptionRequest request) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));
        userSubscriptionMapper.updateEntityFromDto(request, userSubscription);
        return userSubscriptionMapper.toDto(userSubscriptionRepository.save(userSubscription), parkingLotClient, vehicleService);
    }

    @Override
    public void delete(Long id) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SUBSCRIPTION_NOT_FOUND, id));
        userSubscription.setStatus(UserSubscriptionStatus.CANCELLED);
        userSubscriptionRepository.save(userSubscription);
    }

    private boolean checkVehicleUser(Long vehicleId, Long userId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> vehicle.getUser().getId().equals(userId))
                .orElse(false);
    }
}

package com.parkmate.vehicle;

import com.parkmate.account.Account;
import com.parkmate.account.AccountRepository;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
import com.parkmate.vehicle.dto.VehicleSearchCriteria;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public VehicleResponse findById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));
        return vehicleMapper.toDTO(vehicle);
    }

    @Override
    public VehicleResponse findByLicensePlate(String licensePlate) {
        return null;
    }

    @Override
    public VehicleResponse createVehicle(CreateVehicleRequest request, String userId) {

        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new AppException(ErrorCode.VEHICLE_ALREADY_EXISTS, request.getLicensePlate());
        }

        if (userId != null && !userId.isEmpty()) {
            Account account = accountRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, userId));
            request.setUserId(account.getUser().getId());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.getUserId()));

        Vehicle vehicle = vehicleMapper.toEntity(request);

        vehicle.setUser(user);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toDTO(savedVehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));

        vehicleMapper.updateEntityFromDTO(request, vehicle);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Override
    public Page<VehicleResponse> findAll(int page,
                                         int size,
                                         String sortBy,
                                         String sortOrder,
                                         VehicleSearchCriteria searchCriteria,
                                         String accountIdHeader) {
        Long userId = null;
        // X-User-Id header contains accountId, need to convert to userId
        if (accountIdHeader != null && !accountIdHeader.isEmpty() && searchCriteria.isOwnedByMe()) {
            Account account = accountRepository.findById(Long.parseLong(accountIdHeader))
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, accountIdHeader));
            userId = account.getUser().getId();
        }

        System.out.println("DEBUG - accountId header: " + accountIdHeader);
        System.out.println("DEBUG - converted userId: " + userId);
        System.out.println("DEBUG - searchCriteria: " + searchCriteria);

        Predicate predicate = VehicleSpecification.buildPredicate(searchCriteria, userId);
        System.out.println("DEBUG - predicate: " + predicate);

        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        Page<Vehicle> vehiclePage = vehicleRepository.findAll(predicate, pageable);
        System.out.println("DEBUG - total elements: " + vehiclePage.getTotalElements());

        return vehiclePage.map(vehicleMapper::toDTO);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }


}

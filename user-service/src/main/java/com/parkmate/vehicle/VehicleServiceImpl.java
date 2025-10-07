package com.parkmate.vehicle;

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
    public VehicleResponse createVehicle(CreateVehicleRequest request) {

        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new AppException(ErrorCode.VEHICLE_ALREADY_EXISTS, request.licensePlate());
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.userId()));

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
                                         String userId) {
        long uId = 0;
        if (userId != null && !userId.isEmpty()) {
            uId = Long.parseLong(userId);
        }
        Predicate predicate = VehicleSpecification.buildPredicate(searchCriteria, uId);
        Pageable pageable = PaginationUtil.parsePageable(page, size, sortBy, sortOrder);
        Page<Vehicle> vehiclePage = vehicleRepository.findAll(predicate, pageable);
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

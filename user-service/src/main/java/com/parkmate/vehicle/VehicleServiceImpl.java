package com.parkmate.vehicle;

import com.parkmate.user.User;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.user.UserRepository;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final UserRepository userRepository;

    @Override
    public VehicleResponse findById(UUID id) {
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
    public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));

        vehicleMapper.updateEntityFromDTO(request, vehicle);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(updatedVehicle);
    }

    @Override
    public Page<VehicleResponse> findAll(int page, int size, String sort) {
        // để null ở đây tại vì chấp nhận tất cả các field
        // sẽ set valid field sau, nếu không có valid field thì sẽ gây nổ db
        Pageable pageable = PageRequest.of(page, size, PaginationUtil.parseSort(sort, null));

        Page<Vehicle> vehiclePage = vehicleRepository.findAll(pageable);

        return vehiclePage.map(vehicleMapper::toDTO);
    }

    @Override
    public void deleteVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND, id));

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }


}

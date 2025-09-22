package com.parkmate.service.impl;

import com.parkmate.dto.request.CreateVehicleRequest;
import com.parkmate.dto.request.UpdateVehicleRequest;
import com.parkmate.dto.response.VehicleResponse;
import com.parkmate.entity.User;
import com.parkmate.entity.Vehicle;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.mapper.VehicleMapper;
import com.parkmate.repository.UserRepository;
import com.parkmate.repository.VehicleRepository;
import com.parkmate.service.VehicleService;
import com.parkmate.util.PaginationUtil;
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

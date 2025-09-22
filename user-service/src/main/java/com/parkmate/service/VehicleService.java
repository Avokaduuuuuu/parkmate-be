package com.parkmate.service;

import com.parkmate.dto.request.CreateVehicleRequest;
import com.parkmate.dto.request.UpdateVehicleRequest;
import com.parkmate.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface VehicleService {

    VehicleResponse findById(UUID id);

    VehicleResponse findByLicensePlate(String licensePlate);

    VehicleResponse createVehicle(CreateVehicleRequest request);

    VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request);

    Page<VehicleResponse> findAll(int page, int size, String sort);

    void deleteVehicle(UUID id);

}

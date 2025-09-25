package com.parkmate.vehicle;

import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
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

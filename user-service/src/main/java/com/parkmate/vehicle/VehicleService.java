package com.parkmate.vehicle;

import com.parkmate.vehicle.dto.CreateVehicleRequest;
import com.parkmate.vehicle.dto.UpdateVehicleRequest;
import com.parkmate.vehicle.dto.VehicleResponse;
import com.parkmate.vehicle.dto.VehicleSearchCriteria;
import org.springframework.data.domain.Page;

public interface VehicleService {

    VehicleResponse findById(Long id);

    VehicleResponse findByLicensePlate(String licensePlate);

    VehicleResponse createVehicle(CreateVehicleRequest request);

    VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request);

    Page<VehicleResponse> findAll(int page, int size, String sortBy, String sortOrder, VehicleSearchCriteria searchCriteria);

    void deleteVehicle(Long id);

}

package com.parkmate.vehicle;

import com.parkmate.vehicle.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleService {

    VehicleResponse findById(Long id);

    VehicleResponse findByLicensePlate(String licensePlate);

    VehicleResponse createVehicle(CreateVehicleRequest request, String userId);

    VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request);

    Page<VehicleResponse> findAll(int page, int size, String sortBy, String sortOrder, VehicleSearchCriteria searchCriteria, String userId);

    void deleteVehicle(Long id);

    ImportVehicleResponse importVehiclesFromExcel(MultipartFile file);

    long count();

    void exportVehiclesToExcel(VehicleSearchCriteria searchCriteria, String userId, java.io.OutputStream outputStream) throws java.io.IOException;

}

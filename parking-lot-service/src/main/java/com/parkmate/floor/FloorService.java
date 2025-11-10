package com.parkmate.floor;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.floor.dto.resp.FloorDetailedResponse;
import com.parkmate.floor.dto.resp.FloorResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface FloorService {
    FloorResponse createFloor(Long parkingLotId, FloorCreateRequest request);
    FloorDetailedResponse getFloorById(Long parkingLotId);

    Page<FloorResponse> findAll(int page, int size, String sortBy, String sortOrder, FloorFilterParams params);
    void deleteFloor(Long id);

    FloorResponse updateFloor(Long id, FloorUpdateRequest request);

    FloorDetailedResponse getFloorByIdAndVehicleType(Long id, VehicleType vehicleType);

    List<FloorResponse> getSubscriptionAvailability(
            Long parkingLotId,
            VehicleType vehicleType,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    Long count();
}

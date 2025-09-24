package com.parkmate.service;

import com.parkmate.dto.req.ParkingFloorCreateRequest;
import com.parkmate.dto.req.ParkingFloorUpdateRequest;
import com.parkmate.dto.resp.ParkingFloorResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ParkingFloorService {
    ParkingFloorResponse createFloor(Long parkingLotId, ParkingFloorCreateRequest request);
    ParkingFloorResponse getFloorById(Long parkingLotId);

    Page<ParkingFloorResponse> findAll(int page, int size, String sortBy, String sortOrder);
    ParkingFloorResponse deleteFloor(Long id);

    ParkingFloorResponse updateFloor(Long id, ParkingFloorUpdateRequest request);
}

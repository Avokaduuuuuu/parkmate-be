package com.parkmate.floor;

import com.parkmate.floor.dto.req.FloorCreateRequest;
import com.parkmate.floor.dto.req.FloorUpdateRequest;
import com.parkmate.floor.dto.resp.FloorResponse;
import org.springframework.data.domain.Page;

public interface FloorService {
    FloorResponse createFloor(Long parkingLotId, FloorCreateRequest request);
    FloorResponse getFloorById(Long parkingLotId);

    Page<FloorResponse> findAll(int page, int size, String sortBy, String sortOrder, FloorFilterParams params);
    FloorResponse deleteFloor(Long id);

    FloorResponse updateFloor(Long id, FloorUpdateRequest request);
}

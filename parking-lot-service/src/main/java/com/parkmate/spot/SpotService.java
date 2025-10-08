package com.parkmate.spot;


import com.parkmate.spot.dto.req.SpotCreateRequest;
import com.parkmate.spot.dto.req.SpotUpdateRequest;
import com.parkmate.spot.dto.resp.SpotResponse;

import java.util.List;

public interface SpotService {
    SpotResponse findById(Long id);
    List<SpotResponse> findAll(
        SpotFilterParams params
    );

    List<SpotResponse> addSpots(List<SpotCreateRequest> requests, Long areaId);
    SpotResponse updateSpot(Long id, SpotUpdateRequest request);
    SpotResponse deleteSpot(Long id);

    Long count();
}

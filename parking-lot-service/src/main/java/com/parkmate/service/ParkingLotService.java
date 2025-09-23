package com.parkmate.service;

import com.parkmate.dto.req.ParkingLotCreateRequest;
import com.parkmate.dto.req.ParkingLotUpdateRequest;
import com.parkmate.dto.resp.ParkingLotResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ParkingLotService {
    Page<ParkingLotResponse> fetchAllParkingLots(
            int page,
            int size,
            String sortBy,
            String sortOrder
    );

    ParkingLotResponse getParkingLotById(Long id);
    ParkingLotResponse addParkingLot(ParkingLotCreateRequest request);
    ParkingLotResponse updateParkingLot(Long id, ParkingLotUpdateRequest request);
    ParkingLotResponse deleteParkingLot(Long id);
}

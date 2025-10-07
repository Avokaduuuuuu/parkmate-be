package com.parkmate.parking_lot;

import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.parking_lot.dto.resp.ParkingLotDetailedResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface ParkingLotService {
    Page<ParkingLotResponse> fetchAllParkingLots(
            String userHeaderId,
            int page,
            int size,
            String sortBy,
            String sortOrder,
            ParkingLotFilterParams params
    );

    ParkingLotDetailedResponse getParkingLotById(Long id);
    ParkingLotResponse addParkingLot(String userHeaderId, ParkingLotCreateRequest request);
    ParkingLotResponse updateParkingLot(Long id, ParkingLotUpdateRequest request);
    ParkingLotResponse deleteParkingLot(Long id);
}

package com.parkmate.parking_lot;

import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.parking_lot.dto.resp.ParkingLotDetailedResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotResponse;
import org.springframework.data.domain.Page;

public interface ParkingLotService {
    Page<ParkingLotResponse> fetchAllParkingLots(
            int page,
            int size,
            String sortBy,
            String sortOrder
    );

    ParkingLotDetailedResponse getParkingLotById(Long id);
    ParkingLotResponse addParkingLot(Long partnerId,ParkingLotCreateRequest request);
    ParkingLotResponse updateParkingLot(Long id, ParkingLotUpdateRequest request);
    ParkingLotResponse deleteParkingLot(Long id);
}

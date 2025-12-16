package com.parkmate.parking_lot;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.dto.req.ParkingLotCreateRequest;
import com.parkmate.parking_lot.dto.req.ParkingLotUpdateRequest;
import com.parkmate.parking_lot.dto.resp.ParkingLotAvailableReservationSpotResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotBasicInfo;
import com.parkmate.parking_lot.dto.resp.ParkingLotDetailedResponse;
import com.parkmate.parking_lot.dto.resp.ParkingLotResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    ParkingLotDetailedResponse getParkingLotByIdAndVehicleType(Long id, VehicleType vehicleType);

    Long count();

    ParkingLotAvailableReservationSpotResponse countAvailableSpot(
            Long id, LocalDateTime reservedFrom, Integer assumedStayMinute, VehicleType vehicleType
    );

//    ParkingLotAvailableReservationSpotResponse countAvailableSpot(
//            Long id, LocalDateTime reservedFrom, Integer assumedStayMinute, VehicleType vehicleType, Boolean isElectric
//    );

    boolean supportsVehicleType(Long parkingLotId, VehicleType vehicleType);

    /**
     * Activates a parking lot (called from payment-service after payment confirmation)
     *
     * @param lotId The parking lot ID to activate
     */
    void activateParkingLot(Long lotId);

    /**
     * Get all parking lots with basic info (id, name, partnerId)
     * Used by payment-service for withdrawal period creation
     *
     * @return List of all parking lots basic info
     */
    List<ParkingLotBasicInfo> getAllParkingLotsBasicInfo();

    /**
     * Get parking lots by partner ID with basic info
     * Used by payment-service for partner-specific revenue calculations
     *
     * @param partnerId The partner ID
     * @return List of parking lots for the partner
     */
    List<ParkingLotBasicInfo> getParkingLotsByPartner(Long partnerId);

    Map<Long, Long> getParkingLotCountByPartner(List<Long> partnerIds);
}

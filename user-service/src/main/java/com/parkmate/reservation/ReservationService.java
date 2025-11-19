package com.parkmate.reservation;

import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.reservation.dto.*;
import com.parkmate.vehicle.VehicleType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {

    ReservationResponse createReservation(CreateReservationRequest request, String userId);

    ReservationResponse getReservationById(Long id);

    void cancelReservation(Long id, String userIdHeader);

    Page<ReservationResponse> getReservations(int page, int size, String sortBy, String sortOrder, ReservationSearchCriteria criteria, String userIdHeader);

    List<SyncReservationResponse> getReservationForSyncing(Long lotId, ReservationStatus status);

    void updateReservation(Long id, SyncReservationUpdateRequest request);

    Long checkOverlap(Long parkingLotId, LocalDateTime start, Integer assumedStayMinute, VehicleType vehicleType);
}

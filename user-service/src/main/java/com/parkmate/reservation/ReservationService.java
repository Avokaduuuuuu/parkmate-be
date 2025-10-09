package com.parkmate.reservation;

import com.parkmate.reservation.dto.CreateReservationRequest;
import com.parkmate.reservation.dto.ReservationResponse;
import com.parkmate.reservation.dto.ReservationSearchCriteria;
import org.springframework.data.domain.Page;

public interface ReservationService {

    ReservationResponse createReservation(CreateReservationRequest request, String userId);

    ReservationResponse getReservationById(Long id);

    void cancelReservation(Long id);

    Page<ReservationResponse> getReservations(int page, int size, String sortBy, String sortOrder, ReservationSearchCriteria criteria, String userIdHeader);
}

package com.parkmate.reservation;

import com.parkmate.common.enums.ReservationStatus;
import com.parkmate.reservation.dto.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReservationService {

    ReservationResponse createReservation(CreateReservationRequest request, String userId);

    ReservationResponse getReservationById(Long id);

    void cancelReservation(Long id);

    Page<ReservationResponse> getReservations(int page, int size, String sortBy, String sortOrder, ReservationSearchCriteria criteria, String userIdHeader);

    List<SyncReservationResponse> getReservationForSyncing(Long lotId, ReservationStatus status);

    void updateReservation(Long id, SyncReservationUpdateRequest request);

    Map<Long, Boolean> checkOverlap(List<Long> spotIds,
                                    LocalDateTime start,
                                    LocalDateTime end);
}

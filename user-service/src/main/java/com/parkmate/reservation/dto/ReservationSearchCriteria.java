package com.parkmate.reservation.dto;

import com.parkmate.common.enums.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ReservationSearchCriteria {

    boolean ownedByMe;

    Long id;

    Long userId;

    Long vehicleId;

    Long parkingLotId;

    Long spotId;

    ReservationStatus status;

    LocalDateTime startDate;

    LocalDateTime createdAfter;

    LocalDateTime createdBefore;


}

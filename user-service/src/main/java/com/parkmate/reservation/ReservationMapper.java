package com.parkmate.reservation;

import com.parkmate.common.config.MapStructConfig;
import com.parkmate.reservation.dto.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ReservationMapper {

    @Mapping(target = "qrCode", ignore = true)
    ReservationResponse toResponse(Reservation reservation);

}

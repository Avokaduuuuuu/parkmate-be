package com.parkmate.reservation;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.common.config.MapStructConfig;
import com.parkmate.reservation.dto.ReservationResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ReservationMapper {

    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "parkingLotName",
            expression = "java(getParkingLotName(parkingLotClient, reservation.getParkingLotId()))")
    @Mapping(target = "spotName",
            expression = "java(getSpotName(parkingLotClient, reservation.getSpotId()))")
    ReservationResponse toResponse(Reservation reservation, @Context ParkingLotClient parkingLotClient);

    default String getParkingLotName(ParkingLotClient client, Long parkingLotId) {
        try {
            var response = client.getParkingLotName(parkingLotId);
            return response != null && response.data() != null ? response.data().name() : null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    default String getSpotName(ParkingLotClient client, Long spotId) {
        try {
            var response = client.getSpotName(spotId);
            return response != null && response.data() != null ? response.data().name() : null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

}

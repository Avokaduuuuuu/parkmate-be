package com.parkmate.reservation;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.common.config.MapStructConfig;
import com.parkmate.reservation.dto.ReservationResponse;
import com.parkmate.reservation.dto.SyncReservationResponse;
import com.parkmate.user.UserService;
import com.parkmate.user.dto.UserResponse;
import com.parkmate.vehicle.VehicleService;
import com.parkmate.vehicle.VehicleType;
import com.parkmate.vehicle.dto.VehicleResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ReservationMapper {

    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "parkingLotName",
            expression = "java(getParkingLotName(parkingLotClient, reservation.getParkingLotId()))")
    @Mapping(target = "vehicleLicensePlate",
            expression = "java(getVehicleLicensePlate(reservation.getVehicle(), vehicleService))")
    @Mapping(target = "vehicleType",
            expression = "java(getVehicleType(reservation.getVehicle(), vehicleService))")
    @Mapping(target = "userId", expression = "java(getUserId(reservation))")
    @Mapping(target = "vehicleId", expression = "java(getVehicleId(reservation))")
    ReservationResponse toResponse(Reservation reservation,
                                   @Context ParkingLotClient parkingLotClient,
                                   @Context VehicleService vehicleService);

    @Mapping(target = "fullName",
            expression = "java(getUserFullName(getUserId(reservation), userService))")
    @Mapping(target = "licensePlate",
            expression = "java(getVehicleLicensePlate(reservation.getVehicle(), vehicleService))")
    @Mapping(target = "vehicleType",
            expression = "java(getVehicleType(reservation.getVehicle(), vehicleService))")
    @Mapping(target = "userId", expression = "java(getUserId(reservation))")
    @Mapping(target = "vehicleId", expression = "java(getVehicleId(reservation))")
    SyncReservationResponse toSyncResponse(Reservation reservation,
                                           @Context UserService userService,
                                           @Context VehicleService vehicleService);

    default String getParkingLotName(ParkingLotClient client, Long parkingLotId) {
        try {
            var response = client.getParkingLotName(parkingLotId);
            return response != null && response.data() != null ? response.data().name() : null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    default String getUserFullName(Long userId, UserService userService) {
        try {
            if (userId == null || userService == null) {
                return null;
            }
            UserResponse userResponse = userService.getUserById(userId);
            if (userResponse != null) {
                return userResponse.firstName() + " " + userResponse.lastName();
            }
            return null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    default Long getUserId(Reservation reservation) {
        if (reservation == null || reservation.getUser() == null) {
            return null;
        }
        return reservation.getUser().getId();
    }

    default Long getVehicleId(Reservation reservation) {
        if (reservation == null || reservation.getVehicle() == null) {
            return null;
        }
        return reservation.getVehicle().getId();
    }

    default String getVehicleLicensePlate(com.parkmate.vehicle.Vehicle vehicle, VehicleService vehicleService) {
        try {
            if (vehicle == null) {
                return null;
            }
            Long vehicleId = vehicle.getId();
            if (vehicleId == null || vehicleService == null) {
                return null;
            }
            VehicleResponse vehicleResponse = vehicleService.findById(vehicleId);
            if (vehicleResponse != null) {
                return vehicleResponse.getLicensePlate();
            }
            return null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    default VehicleType getVehicleType(com.parkmate.vehicle.Vehicle vehicle, VehicleService vehicleService) {
        try {
            if (vehicle == null) {
                return null;
            }
            Long vehicleId = vehicle.getId();
            if (vehicleId == null || vehicleService == null) {
                return null;
            }
            VehicleResponse vehicleResponse = vehicleService.findById(vehicleId);
            if (vehicleResponse != null) {
                return vehicleResponse.getVehicleType();
            }
            return null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

}

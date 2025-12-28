package com.parkmate.userSubscription;

import com.parkmate.client.ParkingLotClient;
import com.parkmate.common.config.MapStructConfig;
import com.parkmate.userSubscription.dto.CreateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UpdateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UserSubscriptionResponse;
import com.parkmate.userSubscription.dto.UserSubscriptionSyncResponse;
import com.parkmate.vehicle.VehicleService;
import com.parkmate.vehicle.dto.VehicleResponse;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Mapper(config = MapStructConfig.class)
public abstract class UserSubscriptionMapper {

    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "daysRemaining", ignore = true)
    @Mapping(target = "parkingLotName", ignore = true)
    @Mapping(target = "assignedSpotName", ignore = true)
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLicensePlate", ignore = true)
    @Mapping(target = "vehicleType", ignore = true)
    @Mapping(target = "subscriptionPackageName", ignore = true)
    @Mapping(target = "needRenewalDecision", ignore = true)
    @Mapping(target = "passedHalfPeriod", ignore = true)
    @Mapping(target = "hasBeenUsed", ignore = true)
    public abstract UserSubscriptionResponse toDto(UserSubscription userSubscription,
                                                   @Context ParkingLotClient parkingLotClient,
                                                   @Context VehicleService vehicleService);

    @AfterMapping
    protected void enrichResponse(@MappingTarget UserSubscriptionResponse response,
                                  UserSubscription entity,
                                  @Context ParkingLotClient parkingLotClient,
                                  @Context VehicleService vehicleService) {
        if (entity.getStartDate() != null && entity.getEndDate() != null) {
            long daysRemaining = calculateDaysRemaining(entity.getStartDate(), entity.getEndDate());
            response.setDaysRemaining(daysRemaining);
            if (daysRemaining <= 7 && entity.getAutoRenew() != null && !entity.getAutoRenew()) {
                response.setNeedRenewalDecision(true);
            }

            // Calculate passedHalfPeriod (use Vietnam timezone UTC+7)
            response.setPassedHalfPeriod(checkPassedHalfPeriod(entity.getStartDate(), entity.getEndDate()));
        }

        if (entity.getParkingLotId() != null && parkingLotClient != null) {
            response.setParkingLotName(fetchParkingLotName(parkingLotClient, entity.getParkingLotId()));
        }

        if (entity.getSubscriptionPackageId() != null && parkingLotClient != null) {
            response.setSubscriptionPackageName(fetchSubscriptionPackage(parkingLotClient, entity.getSubscriptionPackageId()));
        }

        if (entity.getAssignedSpotId() != null && parkingLotClient != null) {
            response.setAssignedSpotName(fetchSpotName(parkingLotClient, entity.getAssignedSpotId()));
        }

        if (entity.getVehicle() != null && entity.getVehicle().getId() != null && vehicleService != null) {
            enrichVehicleInfo(response, entity.getVehicle().getId(), vehicleService);
        }

        // Check hasBeenUsed via parking-lot-service
        if (entity.getId() != null && parkingLotClient != null) {
            response.setHasBeenUsed(checkHasBeenUsed(parkingLotClient, entity.getId()));
        }

    }

    @Mapping(target = "assignedSpotName", ignore = true)
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLicensePlate", ignore = true)
    @Mapping(target = "vehicleType", ignore = true)
    public abstract UserSubscriptionSyncResponse toSyncDto(UserSubscription userSubscription,
                                                           @Context ParkingLotClient parkingLotClient,
                                                           @Context VehicleService vehicleService);

    @AfterMapping
    protected void enrichSyncResponse(@MappingTarget UserSubscriptionSyncResponse response,
                                      UserSubscription entity,
                                      @Context ParkingLotClient parkingLotClient,
                                      @Context VehicleService vehicleService) {

        // Get assigned spot name from external service
        if (entity.getAssignedSpotId() != null && parkingLotClient != null) {
            response.setAssignedSpotName(fetchSpotName(parkingLotClient, entity.getAssignedSpotId()));
        }

        // Get vehicle information
        if (entity.getVehicle() != null && entity.getVehicle().getId() != null && vehicleService != null) {
            enrichVehicleInfo(response, entity.getVehicle().getId(), vehicleService);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "paymentTransactionId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "refundAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "autoRenew", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    public abstract UserSubscription toEntity(CreateUserSubscriptionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "subscriptionPackageId", ignore = true)
    @Mapping(target = "parkingLotId", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "paymentTransactionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    public abstract void updateEntityFromDto(UpdateUserSubscriptionRequest request, @MappingTarget UserSubscription entity);

    protected Long calculateDaysRemaining(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    protected String fetchParkingLotName(ParkingLotClient client, Long parkingLotId) {
        try {
            var response = client.getParkingLotName(parkingLotId);
            return response != null && response.data() != null ? response.data().name() : null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    protected String fetchSpotName(ParkingLotClient client, Long spotId) {
        try {
            if (spotId == null) {
                return null;
            }
            var response = client.getSpotName(spotId);
            return response != null && response.data() != null ? response.data().name() : null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    protected String fetchSubscriptionPackage(ParkingLotClient client, Long subscriptionPackageId) {
        try {
            if (subscriptionPackageId == null) {
                return null;
            }
            var response = client.getSubscription(subscriptionPackageId);
            return response != null && response.data() != null ? response.data().name() : null;
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
    }

    protected void enrichVehicleInfo(Object response, Long vehicleId, VehicleService vehicleService) {
        try {
            if (vehicleId == null || vehicleService == null) {
                return;
            }

            VehicleResponse vehicleResponse = vehicleService.findById(vehicleId);
            if (vehicleResponse == null) {
                return;
            }

            if (response instanceof UserSubscriptionResponse) {
                ((UserSubscriptionResponse) response).setVehicleLicensePlate(vehicleResponse.getLicensePlate());
                if (vehicleResponse.getVehicleType() != null)
                    ((UserSubscriptionResponse) response).setVehicleType(vehicleResponse.getVehicleType().name());
            }

            if (response instanceof UserSubscriptionSyncResponse) {
                ((UserSubscriptionSyncResponse) response).setVehicleLicensePlate(vehicleResponse.getLicensePlate());
                if (vehicleResponse.getVehicleType() != null)
                    ((UserSubscriptionSyncResponse) response).setVehicleType(vehicleResponse.getVehicleType().name());
            }
        } catch (Exception e) {
            // Log error and continue
        }
    }

    /**
     * Check if more than 1/2 of subscription period has passed
     * Uses Vietnam timezone (UTC+7)
     */
    protected Boolean checkPassedHalfPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            if (startDate == null || endDate == null) {
                return null;
            }

            LocalDateTime vietnamNow = LocalDateTime.now().plusHours(7);

            // If subscription hasn't started yet
            if (vietnamNow.isBefore(startDate)) {
                return false;
            }

            long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
            long halfDays = totalDays / 2;
            LocalDateTime halfPoint = startDate.plusDays(halfDays);

            return vietnamNow.isAfter(halfPoint);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if subscription has been used (has any parking session)
     * Calls parking-lot-service via Feign
     */
    protected Boolean checkHasBeenUsed(ParkingLotClient parkingLotClient, Long subscriptionId) {
        try {
            if (subscriptionId == null || parkingLotClient == null) {
                return null;
            }

            var response = parkingLotClient.checkSubscriptionUsage(subscriptionId);
            if (response != null && response.success()) {
                return response.data();
            }
            return null;
        } catch (Exception e) {
            // Log error and return null (unknown)
            return null;
        }
    }

}

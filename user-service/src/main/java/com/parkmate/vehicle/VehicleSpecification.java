package com.parkmate.vehicle;

import com.parkmate.vehicle.dto.VehicleSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VehicleSpecification {

    public static Predicate buildPredicate(VehicleSearchCriteria criteria, Long userId) {

        QVehicle vehicle = QVehicle.vehicle;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }

        if (userId != null) {
            builder.and(vehicle.user.id.eq(userId));
        }

        // User filters
        if (criteria.getUserId() != null) {
            builder.and(vehicle.user.id.eq(criteria.getUserId()));
        }


        // Vehicle type filters
        if (criteria.getVehicleType() != null) {
            builder.and(vehicle.vehicleType.eq(criteria.getVehicleType()));
        }

        // License plate search (partial match)
        if (StringUtils.hasText(criteria.getLicensePlate())) {
            builder.and(vehicle.licensePlate.containsIgnoreCase(criteria.getLicensePlate()));
        }

        // Vehicle brand search (partial match)
        if (StringUtils.hasText(criteria.getVehicleBrand())) {
            builder.and(vehicle.vehicleBrand.containsIgnoreCase(criteria.getVehicleBrand()));
        }

        // Vehicle model search (partial match)
        if (StringUtils.hasText(criteria.getVehicleModel())) {
            builder.and(vehicle.vehicleModel.containsIgnoreCase(criteria.getVehicleModel()));
        }

        // Vehicle color search (partial match)
        if (StringUtils.hasText(criteria.getVehicleColor())) {
            builder.and(vehicle.vehicleColor.containsIgnoreCase(criteria.getVehicleColor()));
        }

        // Active status filter
        if (criteria.getIsActive() != null) {
            builder.and(vehicle.isActive.eq(criteria.getIsActive()));
        }

        // Electric vehicle filter
        if (criteria.getIsElectric() != null) {
            builder.and(vehicle.isElectric.eq(criteria.getIsElectric()));
        }

        // Created date range
        if (criteria.getCreatedAfter() != null) {
            builder.and(vehicle.createdAt.after(criteria.getCreatedAfter()));
        }

        if (criteria.getCreatedBefore() != null) {
            builder.and(vehicle.createdAt.before(criteria.getCreatedBefore()));
        }

        // Updated date range
        if (criteria.getUpdatedAfter() != null) {
            builder.and(vehicle.updatedAt.after(criteria.getUpdatedAfter()));
        }

        if (criteria.getUpdatedBefore() != null) {
            builder.and(vehicle.updatedAt.before(criteria.getUpdatedBefore()));
        }

        return builder;
    }

}

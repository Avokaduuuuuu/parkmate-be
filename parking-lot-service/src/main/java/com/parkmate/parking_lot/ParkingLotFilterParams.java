package com.parkmate.parking_lot;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.lot_capacity.LotCapacityEntity;
import com.parkmate.parking_lot.enums.ParkingLotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Filter parameters for querying parking lots with various criteria including location, capacity, and status")
public class ParkingLotFilterParams {

    @Schema(
            description = "Filter to show only parking lots owned by the authenticated partner. " +
                    "Requires partner authentication. When true, returns only lots belonging to the current partner.",
            example = "true"
    )
    Boolean ownedByMe;

    @Schema(
            description = "Filter parking lots by name using partial match (case-insensitive). " +
                    "Searches for lots whose name contains the provided string.",
            example = "Vincom"
    )
    String name;

    @Schema(
            description = "Filter parking lots by city using partial match. " +
                    "Searches for lots located in cities matching the provided string.",
            example = "Hồ Chí Minh"
    )
    String city;

    @Schema(
            description = "Filter parking lots by 24-hour operation status. " +
                    "True returns only lots operating 24/7, false returns lots with limited hours.",
            example = "true"
    )
    Boolean is24Hour;

    @Schema(
            description = "Filter parking lots by their current operational status",
            example = "ACTIVE",
            allowableValues = {
                    "PENDING",
                    "UNDER_SURVEY",
                    "PREPARING",
                    "PARTNER_CONFIGURATION",
                    "ACTIVE_PENDING",
                    "ACTIVE",
                    "INACTIVE",
                    "UNDER_MAINTENANCE",
                    "MAP_DENIED",
                    "REJECTED"
            }
    )
    ParkingLotStatus status;

    @Schema(
            description = "Latitude coordinate for location-based search. " +
                    "Must be used together with longitude and radiusKm to filter by geographic proximity. " +
                    "Valid range: -90 to 90 degrees.",
            example = "10.7756800",
            minimum = "-90",
            maximum = "90"
    )
    Double latitude;

    @Schema(
            description = "Longitude coordinate for location-based search. " +
                    "Must be used together with latitude and radiusKm to filter by geographic proximity. " +
                    "Valid range: -180 to 180 degrees.",
            example = "106.7004200",
            minimum = "-180",
            maximum = "180"
    )
    Double longitude;

    @Schema(
            description = "Search radius in kilometers for location-based filtering. " +
                    "Returns parking lots within this distance from the specified latitude/longitude. " +
                    "Must be used together with latitude and longitude. " +
                    "Uses approximation: 1 degree ≈ 111 km at equator.",
            example = "5.0",
            minimum = "0"
    )
    Double radiusKm;

    @Schema(
            description = "Filter parking lots by supported vehicle type. " +
                    "Returns only lots that have capacity for the specified vehicle type.",
            example = "CAR_UP_TO_9_SEATS",
            allowableValues = {"BIKE", "MOTORBIKE", "CAR_UP_TO_9_SEATS", "OTHER"}
    )
    VehicleType vehicleType;

    /**
     * Builds a JPA Specification for filtering parking lots based on the provided parameters.
     *
     * @param partnerId The ID of the authenticated partner (required when ownedByMe is true)
     * @return JPA Specification for querying parking lots
     * @throws AppException with UNAUTHORIZED error code if ownedByMe is true but partnerId is null
     */
    public Specification<ParkingLotEntity> getSpecification(Long partnerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by partner ownership
            if (ownedByMe != null && ownedByMe) {
                if (partnerId != null) {
                    predicates.add(cb.equal(root.get("partnerId"), partnerId));
                } else {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
            }

            // Filter by parking lot name (partial match)
            if (name != null) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }

            // Filter by city (partial match)
            if (city != null) {
                predicates.add(cb.like(root.get("city"), "%" + city + "%"));
            }

            // Filter by 24-hour operation status
            if (is24Hour != null) {
                predicates.add(cb.equal(root.get("is24Hour"), is24Hour));
            }

            // Filter by parking lot status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by geographic proximity (bounding box approximation)
            if (latitude != null && longitude != null && radiusKm != null) {
                // Calculate the delta of latitude direction
                // 1 degree latitude = 111km (constant)
                double latDelta = radiusKm / 111.0;

                // Calculate longitude delta (varies by latitude)
                // 1 degree longitude = 111 km * cos(latitude) at given latitude
                // At equator: 111 km, at poles: 0 km
                double longDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

                double minLat = latitude - latDelta;
                double maxLat = latitude + latDelta;
                double minLong = longitude - longDelta;
                double maxLong = longitude + longDelta;

                predicates.add(cb.and(
                        cb.between(root.get("latitude"), minLat, maxLat),
                        cb.between(root.get("longitude"), minLong, maxLong)
                ));
            }

            // Filter by vehicle type capacity
            if (vehicleType != null) {
                Join<ParkingLotEntity, LotCapacityEntity> lotCapacityJoin =
                        root.join("lotCapacity", JoinType.INNER);
                predicates.add(lotCapacityJoin.get("vehicleType").in(vehicleType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
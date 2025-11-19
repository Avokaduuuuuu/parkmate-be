package com.parkmate.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.session.enums.AuthMethod;
import com.parkmate.session.enums.ReferenceType;
import com.parkmate.session.enums.SessionStatus;
import com.parkmate.session.enums.SessionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Filter parameters for querying parking sessions with various criteria including time range, vehicle type, payment status, and authentication method")
public class SessionFilterParams {

    @Schema(
            description = "Filter sessions by parking lot ID. " +
                    "Returns only sessions that occurred at the specified parking lot.",
            example = "1"
    )
    Long lotId;

    @Schema(
            description = "Filter sessions by vehicle type. " +
                    "Returns only sessions for the specified type of vehicle.",
            example = "CAR_UP_TO_9_SEATS",
            allowableValues = {"BIKE", "MOTORBIKE", "CAR_UP_TO_9_SEATS", "OTHER"}
    )
    VehicleType vehicleType;

    @Schema(
            description = "Filter sessions by authentication method used for entry/exit. " +
                    "BLE: Bluetooth Low Energy proximity detection for mobile app users. " +
                    "NFC_CARD: Near Field Communication card for occasional users. " +
                    "QR: QR code scan for reservation-based entry.",
            example = "BLE",
            allowableValues = {"BLE", "NFC_CARD", "QR"}
    )
    AuthMethod authMethod;

    @Schema(
            description = "Filter sessions by user type. " +
                    "MEMBER: Registered users with linked vehicles and wallets. " +
                    "OCCASIONAL: Walk-in users using NFC cards or cash payment.",
            example = "MEMBER",
            allowableValues = {"MEMBER", "OCCASIONAL"}
    )
    SessionType sessionType;

    @Schema(
            description = "Filter sessions by access reference type. " +
                    "WALK_IN: Direct entry without prior reservation or subscription. " +
                    "RESERVATION: Entry based on a pre-booked parking spot. " +
                    "SUBSCRIPTION: Entry using an active parking subscription package.",
            example = "WALK_IN",
            allowableValues = {"WALK_IN", "RESERVATION", "SUBSCRIPTION"}
    )
    ReferenceType referenceType;

    @Schema(
            description = "Filter sessions with entry time on or after this timestamp. " +
                    "Use together with endTime to define a time range for session search. " +
                    "Format: ISO 8601 datetime (yyyy-MM-dd'T'HH:mm:ss)",
            example = "2025-11-01T08:00:00"
    )
    LocalDateTime startTime;

    @Schema(
            description = "Filter sessions with exit time on or before this timestamp. " +
                    "Use together with startTime to define a time range for session search. " +
                    "Format: ISO 8601 datetime (yyyy-MM-dd'T'HH:mm:ss)",
            example = "2025-11-01T18:00:00"
    )
    LocalDateTime endTime;

    @Schema(
            description = "Filter sessions with total amount less than this value. " +
                    "Used to find sessions below a certain cost threshold. " +
                    "Currency: VND (Vietnamese Dong)",
            example = "50000.00",
            minimum = "0"
    )
    Double totalLessThan;

    @Schema(
            description = "Filter sessions with total amount greater than this value. " +
                    "Used to find sessions above a certain cost threshold. " +
                    "Currency: VND (Vietnamese Dong)",
            example = "100000.00",
            minimum = "0"
    )
    Double totalMoreThan;

    @Schema(
            description = "Filter sessions by their current status. " +
                    "ACTIVE: Session is currently in progress (vehicle still parked). " +
                    "COMPLETED: Session has ended (vehicle has exited and payment processed).",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "COMPLETED"}
    )
    SessionStatus sessionStatus;

    @Schema(
            description = "Filter sessions by vehicle license plate (exact match). " +
                    "Searches for sessions associated with the specified license plate number.",
            example = "51A-12345"
    )
    String licensePlate;

    @Schema(
            description = "Filter sessions with parking duration greater than or equal to this value. " +
                    "Used to find long-duration parking sessions. " +
                    "Unit: minutes",
            example = "120",
            minimum = "0"
    )
    Integer durationMinuteGreaterThan;

    @Schema(
            description = "Filter sessions with parking duration less than or equal to this value. " +
                    "Used to find short-duration parking sessions. " +
                    "Unit: minutes",
            example = "30",
            minimum = "0"
    )
    Integer durationMinuteLessThan;

    @Schema(
            description = "Filter to show only sessions owned by the authenticated member. " +
                    "Requires partner authentication. When true, returns only sessions belonging to the current member.",
            example = "true"
    )
    Boolean ownedByMe;

    /**
     * Builds a JPA Specification for filtering parking sessions based on the provided parameters.
     *
     * @return JPA Specification for querying parking sessions
     */
    @JsonIgnore
    public Specification<SessionEntity> getSpecification(Long userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();


            if (ownedByMe != null && ownedByMe) {
                if (userId != null) {
                    predicates.add(cb.equal(root.get("userId"), userId));
                }
            }

            // Filter by parking lot ID
            if (lotId != null) {
                Join<SessionEntity, ParkingLotEntity> parkingLotEntity = root.join("parkingLot", JoinType.LEFT);
                predicates.add(cb.equal(parkingLotEntity.get("id"), lotId));
            }

            // Filter by vehicle type
            if (vehicleType != null) {
                predicates.add(cb.equal(root.get("vehicleType"), vehicleType));
            }

            // Filter by authentication method
            if (authMethod != null) {
                predicates.add(cb.equal(root.get("authMethod"), authMethod));
            }

            // Filter by session type
            if (sessionType != null) {
                predicates.add(cb.equal(root.get("sessionType"), sessionType));
            }

            // Filter by reference type
            if (referenceType != null) {
                predicates.add(cb.equal(root.get("referenceType"), referenceType));
            }

            // Filter by entry time (greater than or equal to startTime)
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("entryTime"), startTime));
            }

            // Filter by exit time (less than or equal to endTime)
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("exitTime"), endTime));
            }

            // Filter by total amount (less than)
            if (totalLessThan != null) {
                predicates.add(cb.lessThan(root.get("totalAmount"), totalLessThan));
            }

            // Filter by total amount (greater than)
            if (totalMoreThan != null) {
                predicates.add(cb.greaterThan(root.get("totalAmount"), totalMoreThan));
            }

            // Filter by session status
            if (sessionStatus != null) {
                predicates.add(cb.equal(root.get("status"), sessionStatus));
            }

            // Filter by license plate (exact match)
            if (licensePlate != null) {
                predicates.add(cb.equal(root.get("licensePlate"), licensePlate));
            }

            // Filter by parking duration (greater than or equal to)
            if (durationMinuteGreaterThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("durationMinute"), durationMinuteGreaterThan));
            }

            // Filter by parking duration (less than or equal to)
            if (durationMinuteLessThan != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("durationMinute"), durationMinuteLessThan));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
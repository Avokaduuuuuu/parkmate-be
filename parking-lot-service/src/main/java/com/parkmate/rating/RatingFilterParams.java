package com.parkmate.rating;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Filter parameters for searching and filtering parking lot ratings")
public class RatingFilterParams {

    @Parameter(
            description = "Filter by parking lot ID - returns only ratings for this specific parking lot",
            example = "1",
            schema = @Schema(type = "integer", format = "int64")
    )
    Long lotId;

    @Parameter(
            description = "Filter by user ID - returns only ratings created by this specific user",
            example = "true",
            schema = @Schema(type = "boolean", format = "int64")
    )
    Boolean ownedByMe;

    @Parameter(
            description = "Filter by exact star rating value (1-5) - returns ratings matching this exact value",
            example = "5",
            schema = @Schema(type = "integer", minimum = "1", maximum = "5")
    )
    Integer overallRating;

    @Parameter(
            description = "Filter ratings less than or equal to this value - useful for finding negative reviews",
            example = "2",
            schema = @Schema(type = "integer", minimum = "1", maximum = "5")
    )
    Integer ratingLessThan;

    @Parameter(
            description = "Filter ratings greater than or equal to this value - useful for finding positive reviews",
            example = "4",
            schema = @Schema(type = "integer", minimum = "1", maximum = "5")
    )
    Integer ratingGreaterThan;

    @Parameter(
            description = "Filter by visibility status - true for visible ratings, false for hidden/moderated ratings",
            example = "true",
            schema = @Schema(type = "boolean")
    )
    Boolean isVisible;

    @Parameter(
            description = "Filter ratings created before this date/time (exclusive) - format: yyyy-MM-dd'T'HH:mm:ss",
            example = "2025-11-01T00:00:00",
            schema = @Schema(type = "string", format = "date-time")
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime createdBefore;

    @Parameter(
            description = "Filter ratings created after this date/time (exclusive) - format: yyyy-MM-dd'T'HH:mm:ss",
            example = "2025-10-01T00:00:00",
            schema = @Schema(type = "string", format = "date-time")
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime createdAfter;

    /**
     * Builds a JPA Specification for filtering RatingEntity based on the provided parameters.
     *
     * <p>This method creates dynamic database queries using JPA Criteria API based on non-null filter parameters.
     * All conditions are combined using AND logic.</p>
     *
     * <p><b>Filter Logic:</b></p>
     * <ul>
     *   <li><b>lotId:</b> Exact match on parking lot ID (INNER JOIN)</li>
     *   <li><b>userId:</b> Exact match on user ID</li>
     *   <li><b>overallRating:</b> Exact match on star rating</li>
     *   <li><b>ratingLessThan:</b> Star rating ≤ specified value (inclusive)</li>
     *   <li><b>ratingGreaterThan:</b> Star rating ≥ specified value (inclusive)</li>
     *   <li><b>isVisible:</b> Exact match on visibility status</li>
     *   <li><b>createdBefore:</b> Created timestamp < specified datetime (exclusive)</li>
     *   <li><b>createdAfter:</b> Created timestamp > specified datetime (exclusive)</li>
     * </ul>
     *
     * <p><b>Example Usage:</b></p>
     * <pre>
     * // Find all 5-star visible ratings for parking lot 1
     * RatingFilterParams params = RatingFilterParams.builder()
     *     .lotId(1L)
     *     .overallRating(5)
     *     .isVisible(true)
     *     .build();
     *
     * // Find negative reviews (2 stars or less) from last 30 days
     * RatingFilterParams params = RatingFilterParams.builder()
     *     .ratingLessThan(2)
     *     .createdAfter(LocalDateTime.now().minusDays(30))
     *     .build();
     * </pre>
     *
     * @return Specification that can be used with JPA repository for filtering
     */
    public Specification<RatingEntity> getSpecification(Long userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by parking lot ID
            if (lotId != null) {
                Join<RatingEntity, ParkingLotEntity> join = root.join("parkingLot", JoinType.LEFT);
                predicates.add(cb.equal(join.get("id"), lotId));
            }

            if (ownedByMe != null && ownedByMe) {
                if (userId != null) {
                    predicates.add(cb.equal(root.get("userId"), userId));
                } else throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            // Filter by exact rating value
            if (overallRating != null) {
                predicates.add(cb.equal(root.get("overallRating"), overallRating));
            }

            // Filter ratings less than or equal to specified value
            if (ratingLessThan != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("overallRating"), ratingLessThan));
            }

            // Filter ratings greater than or equal to specified value
            if (ratingGreaterThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("overallRating"), ratingGreaterThan));
            }

            // Filter by visibility status
            if (isVisible != null) {
                predicates.add(cb.equal(root.get("isVisible"), isVisible));
            }

            // Filter ratings created before specified date/time
            if (createdBefore != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), createdBefore));
            }

            // Filter ratings created after specified date/time
            if (createdAfter != null) {
                predicates.add(cb.greaterThan(root.get("createdAt"), createdAfter));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
package com.parkmate.floor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parkmate.parking_lot.ParkingLotEntity;
import io.swagger.v3.oas.annotations.Parameter;
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
@Schema(description = "Filter parameters for querying parking floors")
public class FloorFilterParams {

    @Schema(
            description = "Filter floors by parking lot ID. Only returns floors belonging to the specified parking lot.",
            example = "1"
    )
    Long parkingLotId;

    @Schema(
            description = "Filter floors by name using case-insensitive partial matching. Searches within the floor_name field.",
            example = "ground"
    )
    String name;

    @Schema(
            description = "Filter floors by active status. True returns only active floors, false returns only inactive floors.",
            example = "true"
    )
    Boolean isActive;

    @JsonIgnore
    public Specification<FloorEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (parkingLotId != null) {
                Join<FloorEntity, ParkingLotEntity> join = root.join("parkingLot", JoinType.LEFT);
                predicates.add(cb.equal(join.get("id"), parkingLotId));
            }

            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("floorName")), "%" + name.toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
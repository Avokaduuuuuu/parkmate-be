package com.parkmate.parking_lot;

import com.parkmate.parking_lot.enums.ParkingLotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Filter parameters for querying parking lots")
public class ParkingLotFilterParams {
    Long partnerId;
    String name;
    String city;
    Boolean is24Hour;
    ParkingLotStatus status;

    public Specification<ParkingLotEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (partnerId != null) {
                predicates.add(cb.equal(root.get("partnerId"), partnerId));
            }
            if (name != null) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (city != null) {
                predicates.add(cb.like(root.get("city"), "%" + city + "%"));
            }
            if (is24Hour != null) {
                predicates.add(cb.equal(root.get("is24Hour"), is24Hour));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

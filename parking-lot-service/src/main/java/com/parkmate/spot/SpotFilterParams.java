package com.parkmate.spot;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parkmate.area.AreaEntity;
import com.parkmate.spot.enums.SpotStatus;
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
public class SpotFilterParams {
    Long areaId;
    String name;
    SpotStatus status;


    @JsonIgnore
    public Specification<SpotEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (areaId != null) {
                Join<AreaEntity, SpotEntity> joinArea = root.join("parkingArea", JoinType.LEFT);
                predicates.add(cb.equal(joinArea.get("id"), areaId));
            }
            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

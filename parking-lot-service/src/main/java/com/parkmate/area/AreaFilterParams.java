package com.parkmate.area;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parkmate.common.enums.VehicleType;
import com.parkmate.floor.FloorEntity;
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
public class AreaFilterParams {
    Long floorId;
    String name;
    VehicleType vehicleType;
    Boolean isActive;
    Boolean supportElectricVehicle;

    @JsonIgnore
    public Specification<AreaEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (floorId != null) {
                Join<FloorEntity, AreaEntity> floor = root.join("parkingFloor", JoinType.LEFT);
                predicates.add(cb.equal(floor.get("id"), floorId));
            }

            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name + "%"));
            }

            if (vehicleType != null) {
                predicates.add(cb.equal(root.get("vehicleType"), vehicleType));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (supportElectricVehicle != null) {
                predicates.add(cb.equal(root.get("supportElectricVehicle"), supportElectricVehicle));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

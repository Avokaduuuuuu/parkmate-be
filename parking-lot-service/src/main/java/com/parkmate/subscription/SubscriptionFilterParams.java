package com.parkmate.subscription;

import com.parkmate.common.enums.VehicleType;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.subscription.enums.DurationType;
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
public class SubscriptionFilterParams {
    String name;
    Boolean isActive;
    VehicleType vehicleType;
    DurationType durationType;
    Long lotId;

    public Specification<SubscriptionEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name + "%"));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (vehicleType != null) {
                predicates.add(cb.equal(root.get("vehicleType"), vehicleType));
            }

            if (durationType != null) {
                predicates.add(cb.equal(root.get("durationType"), durationType));
            }

            if (lotId != null) {
                Join<SubscriptionEntity, ParkingLotEntity> join = root.join("parkingLot", JoinType.LEFT);
                predicates.add(cb.equal(join.get("id"), lotId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

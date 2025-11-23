package com.parkmate.device;

import com.parkmate.device.enums.DeviceStatus;
import com.parkmate.device.enums.DeviceType;
import com.parkmate.parking_lot.ParkingLotEntity;
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
public class DeviceFilterParams {
    Long lotId;
    String deviceId;
    String deviceName;
    String model;
    String serialNumber;
    DeviceType deviceType;
    DeviceStatus deviceStatus;
    Boolean isActive;

    public Specification<DeviceEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (lotId != null) {
                Join<DeviceEntity, ParkingLotEntity> join = root.join("parkingLot", JoinType.INNER);
                predicates.add(cb.equal(join.get("id"), lotId));
            }
            if (deviceId != null) {
                predicates.add(cb.equal(root.get("deviceId"), deviceId));
            }
            if (deviceName != null) {
                predicates.add(cb.like(root.get("deviceName"), "%" + deviceName + "%"));
            }
            if (model != null) {
                predicates.add(cb.like(root.get("model"), "%" + model + "%"));
            }
            if (serialNumber != null) {
                predicates.add(cb.equal(root.get("serialNumber"), serialNumber));
            }
            if (deviceType != null) {
                predicates.add(cb.equal(root.get("deviceType"), deviceType));
            }
            if (deviceStatus != null) {
                predicates.add(cb.equal(root.get("deviceStatus"), deviceStatus));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

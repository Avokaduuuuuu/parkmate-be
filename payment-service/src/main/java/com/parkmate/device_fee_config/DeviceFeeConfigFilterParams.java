package com.parkmate.device_fee_config;

import com.parkmate.common.enums.DeviceType;
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
public class DeviceFeeConfigFilterParams {
    DeviceType deviceType;
    Double deviceFeeLessThan;
    Double deviceFeeGreaterThan;
    Boolean isActive;
    LocalDateTime validFrom;
    LocalDateTime validUntil;

    public Specification<DeviceFeeConfigEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (deviceType != null) {
                predicates.add(cb.equal(root.get("deviceType"), deviceType));
            }
            if (deviceFeeLessThan != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("deviceFee"), deviceFeeLessThan));
            }
            if (deviceFeeGreaterThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deviceFee"), deviceFeeGreaterThan));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (validFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("validFrom"), validFrom));
            }
            if (validUntil != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("validUntil"), validUntil));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

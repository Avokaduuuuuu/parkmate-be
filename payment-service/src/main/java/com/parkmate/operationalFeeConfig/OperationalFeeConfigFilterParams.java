package com.parkmate.operationalFeeConfig;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperationalFeeConfigFilterParams {
    Double pricePerSqmLessThan;
    Double pricePerSqmGreaterThan;
    Integer billingPeriodMonthsLessThan;
    Integer billingPeriodMonthsGreaterThan;
    Boolean isActive;
    LocalDateTime validFrom;
    LocalDateTime validUntil;

    public Specification<OperationalFeeConfigEntity> getSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (pricePerSqmLessThan != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerSqm"), pricePerSqmLessThan));
            }
            if (pricePerSqmGreaterThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerSqm"), pricePerSqmGreaterThan));
            }
            if (billingPeriodMonthsLessThan != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("billingPeriodMonths"), billingPeriodMonthsLessThan));
            }
            if (billingPeriodMonthsGreaterThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("billingPeriodMonths"), billingPeriodMonthsGreaterThan));
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

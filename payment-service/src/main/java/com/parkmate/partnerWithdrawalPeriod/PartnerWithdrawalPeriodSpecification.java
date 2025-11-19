package com.parkmate.partnerWithdrawalPeriod;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PartnerWithdrawalPeriodSpecification {

    public static Specification<PartnerWithdrawalPeriod> filterBy(PartnerWithdrawalPeriodFilterParams filterParams) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterParams.getPartnerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("partnerId"), filterParams.getPartnerId()));
            }

            if (filterParams.getLotId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("lotId"), filterParams.getLotId()));
            }

            if (filterParams.getIsWithdrawn() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isWithdrawn"), filterParams.getIsWithdrawn()));
            }

            if (filterParams.getPeriodLabel() != null && !filterParams.getPeriodLabel().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("periodLabel")),
                        "%" + filterParams.getPeriodLabel().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.parkmate.partnerWithdrawal;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PartnerWithdrawalSpecification {

    public static Specification<PartnerWithdrawal> filterBy(PartnerWithdrawalFilterParams filterParams) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterParams.getPartnerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("partnerId"), filterParams.getPartnerId()));
            }

            if (filterParams.getLotId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("lotId"), filterParams.getLotId()));
            }

            if (filterParams.getStatus() != null && !filterParams.getStatus().isEmpty()) {
                try {
                    WithdrawalStatus status = WithdrawalStatus.valueOf(filterParams.getStatus().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore this filter
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

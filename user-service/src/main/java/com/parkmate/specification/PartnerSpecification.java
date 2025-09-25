package com.parkmate.specification;

import com.parkmate.dto.criteria.PartnerSearchCriteria;
import com.parkmate.entity.QPartner;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;

@Component
public class PartnerSpecification {

    public static Predicate buildPredicate(PartnerSearchCriteria criteria) {
        QPartner partner = QPartner.partner;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }

        // Single values
        if (criteria.getPartnerId() != null) {
            builder.and(partner.id.eq(criteria.getPartnerId()));
        }

        if (criteria.getStatus() != null) {
            builder.and(partner.status.eq(criteria.getStatus()));
        }

        // Multiple values
        if (criteria.getPartnerIds() != null && !criteria.getPartnerIds().isEmpty()) {
            builder.and(partner.id.in(criteria.getPartnerIds()));
        }

        if (criteria.getStatusList() != null && !criteria.getStatusList().isEmpty()) {
            builder.and(partner.status.in(criteria.getStatusList()));
        }

        // Text search (like / equals)
        if (criteria.getCompanyName() != null && !criteria.getCompanyName().isBlank()) {
            builder.and(partner.companyName.containsIgnoreCase(criteria.getCompanyName()));
        }

        if (criteria.getTaxNumber() != null && !criteria.getTaxNumber().isBlank()) {
            builder.and(partner.taxNumber.eq(criteria.getTaxNumber()));
        }

        if (criteria.getBusinessLicenseNumber() != null && !criteria.getBusinessLicenseNumber().isBlank()) {
            builder.and(partner.businessLicenseNumber.eq(criteria.getBusinessLicenseNumber()));
        }

        if (criteria.getCompanyPhone() != null && !criteria.getCompanyPhone().isBlank()) {
            builder.and(partner.companyPhone.containsIgnoreCase(criteria.getCompanyPhone()));
        }

        if (criteria.getCompanyEmail() != null && !criteria.getCompanyEmail().isBlank()) {
            builder.and(partner.companyEmail.containsIgnoreCase(criteria.getCompanyEmail()));
        }

        if (criteria.getCompanyAddress() != null && !criteria.getCompanyAddress().isBlank()) {
            builder.and(partner.companyAddress.containsIgnoreCase(criteria.getCompanyAddress()));
        }

        // Date filters
        if (criteria.getCreatedAfter() != null) {
            builder.and(partner.createdAt.goe(criteria.getCreatedAfter()));
        }

        if (criteria.getCreatedBefore() != null) {
            builder.and(partner.createdAt.loe(criteria.getCreatedBefore()));
        }

        if (criteria.getUpdatedAfter() != null) {
            builder.and(partner.updatedAt.goe(criteria.getUpdatedAfter()));
        }

        if (criteria.getUpdatedBefore() != null) {
            builder.and(partner.updatedAt.loe(criteria.getUpdatedBefore()));
        }

        return builder;
    }
}

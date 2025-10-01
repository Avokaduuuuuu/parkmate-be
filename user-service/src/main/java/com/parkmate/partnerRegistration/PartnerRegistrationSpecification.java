package com.parkmate.partnerRegistration;

import com.parkmate.partnerRegistration.dto.PartnerRegistrationSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.util.StringUtils;

public class PartnerRegistrationSpecification {

    public static Predicate buildPredicate(PartnerRegistrationSearchCriteria criteria) {
        QPartnerRegistration pr = QPartnerRegistration.partnerRegistration;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }

        // Filter by company name (partial match, case-insensitive)
        if (StringUtils.hasText(criteria.getCompanyName())) {
            builder.and(pr.companyName.containsIgnoreCase(criteria.getCompanyName()));
        }

        // Filter by tax number (exact match)
        if (StringUtils.hasText(criteria.getTaxNumber())) {
            builder.and(pr.taxNumber.eq(criteria.getTaxNumber()));
        }

        // Filter by business license number (partial match)
        if (StringUtils.hasText(criteria.getBusinessLicenseNumber())) {
            builder.and(pr.businessLicenseNumber.containsIgnoreCase(criteria.getBusinessLicenseNumber()));
        }

        // Filter by company address (partial match, case-insensitive)
        if (StringUtils.hasText(criteria.getCompanyAddress())) {
            builder.and(pr.companyAddress.containsIgnoreCase(criteria.getCompanyAddress()));
        }

        // Filter by company email (partial match, case-insensitive)
        if (StringUtils.hasText(criteria.getCompanyEmail())) {
            builder.and(pr.companyEmail.containsIgnoreCase(criteria.getCompanyEmail()));
        }

        // Filter by company phone (partial match)
        if (StringUtils.hasText(criteria.getCompanyPhone())) {
            builder.and(pr.companyPhone.contains(criteria.getCompanyPhone()));
        }

        // Filter by contact person name (partial match, case-insensitive)
        if (StringUtils.hasText(criteria.getContactPersonName())) {
            builder.and(pr.contactPersonName.containsIgnoreCase(criteria.getContactPersonName()));
        }

        // Filter by contact person email (partial match, case-insensitive)
        if (StringUtils.hasText(criteria.getContactPersonEmail())) {
            builder.and(pr.contactPersonEmail.containsIgnoreCase(criteria.getContactPersonEmail()));
        }

        // Filter by status
        if (criteria.getStatus() != null) {
            builder.and(pr.status.eq(criteria.getStatus()));
        }

        // Filter by submitted date range
        if (criteria.getSubmittedAfter() != null) {
            builder.and(pr.submittedAt.goe(criteria.getSubmittedAfter()));
        }

        if (criteria.getSubmittedBefore() != null) {
            builder.and(pr.submittedAt.loe(criteria.getSubmittedBefore()));
        }

        // Filter by reviewed date range
        if (criteria.getReviewedAfter() != null) {
            builder.and(pr.reviewedAt.goe(criteria.getReviewedAfter()));
        }

        if (criteria.getReviewedBefore() != null) {
            builder.and(pr.reviewedAt.loe(criteria.getReviewedBefore()));
        }

        // Filter by reviewer ID
        if (criteria.getReviewedBy() != null) {
            builder.and(pr.reviewedBy.eq(criteria.getReviewedBy()));
        }

        // Filter by whether partner has been created
        if (criteria.getHasPartner() != null) {
            if (criteria.getHasPartner()) {
                builder.and(pr.partner.isNotNull());
            } else {
                builder.and(pr.partner.isNull());
            }
        }

        return builder;
    }
}
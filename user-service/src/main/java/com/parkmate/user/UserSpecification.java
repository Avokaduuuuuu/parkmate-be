package com.parkmate.user;

import com.parkmate.user.dto.UserSearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;

@Component
public class UserSpecification {

    public static Predicate buildPredicate(UserSearchCriteria criteria) {

        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria == null) {
            return builder;
        }


        if (criteria.getId() != null) {
            builder.and(user.id.eq(criteria.getId()));
        }

        if (criteria.getPhone() != null) {
            builder.and(user.phone.containsIgnoreCase(criteria.getPhone()));
        }

        if (criteria.getFirstName() != null) {
            builder.and(user.firstName.containsIgnoreCase(criteria.getFirstName()));
        }

        if (criteria.getLastName() != null) {
            builder.and(user.lastName.containsIgnoreCase(criteria.getLastName()));
        }

        if (criteria.getFullName() != null) {
            builder.and(user.fullName.containsIgnoreCase(criteria.getFullName()));
        }

        if (criteria.getAddress() != null) {
            builder.and(user.address.containsIgnoreCase(criteria.getAddress()));
        }

        if (criteria.getIdNumber() != null) {
            builder.and(user.idNumber.eq(criteria.getIdNumber()));
        }

        if (criteria.getDateOfBirthFrom() != null) {
            builder.and(user.dateOfBirth.goe(criteria.getDateOfBirthFrom()));
        }

        if (criteria.getDateOfBirthTo() != null) {
            builder.and(user.dateOfBirth.loe(criteria.getDateOfBirthTo()));
        }

        if (criteria.getCreatedAtFrom() != null) {
            builder.and(user.createdAt.goe(criteria.getCreatedAtFrom()));
        }

        if (criteria.getCreatedAtTo() != null) {
            builder.and(user.createdAt.loe(criteria.getCreatedAtTo()));
        }

        if (criteria.getUpdatedAtFrom() != null) {
            builder.and(user.updatedAt.goe(criteria.getUpdatedAtFrom()));
        }

        if (criteria.getUpdatedAtTo() != null) {
            builder.and(user.updatedAt.loe(criteria.getUpdatedAtTo()));
        }

        if (criteria.getAccountId() != null) {
            builder.and(user.account.id.eq(criteria.getAccountId()));
        }

        return builder;
    }
}

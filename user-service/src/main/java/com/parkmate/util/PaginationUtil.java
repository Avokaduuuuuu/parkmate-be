package com.parkmate.util;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

@NoArgsConstructor
public final class PaginationUtil {

    private static final String DEFAULT_SORT_FIELD = "id";
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    public static Sort parseSort(String sort, Set<String> validFields) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, DEFAULT_SORT_FIELD);
        }

        String[] parts = sort.split(",", 2);
        String sortField = parts[0].trim();
        String sortDirection = parts.length > 1 ? parts[1].trim() : DEFAULT_SORT_DIRECTION;

        if (validFields != null && !validFields.isEmpty() && !validFields.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField +
                    ". Valid fields: " + validFields);
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortField);
    }

    public static Pageable parsePageable(int page, int size, String sort, Set<String> validFields) {
        return PageRequest.of(page, size, parseSort(sort, validFields));
    }
}

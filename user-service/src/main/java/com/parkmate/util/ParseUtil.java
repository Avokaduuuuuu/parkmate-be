package com.parkmate.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ParseUtil {

    public static List<String> parseStringList(String input) {
        if (!StringUtils.hasText(input)) return Collections.emptyList();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    public static List<Long> parseLongList(String input, String fieldName) {
        if (!StringUtils.hasText(input)) return Collections.emptyList();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(id -> {
                    try {
                        return Long.valueOf(id);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid " + fieldName + ": " + id);
                    }
                })
                .collect(Collectors.toList());
    }

    public static <T extends Enum<T>> List<T> parseEnumList(String input, Class<T> enumClass, String fieldName) {
        if (!StringUtils.hasText(input)) return Collections.emptyList();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(value -> {
                    try {
                        return Enum.valueOf(enumClass, value.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
                    }
                })
                .collect(Collectors.toList());
    }

}

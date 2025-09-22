package com.parkmate.dto.response;

public record PageResponse<T>(java.util.List<T> items, long total, int page, int size) {}

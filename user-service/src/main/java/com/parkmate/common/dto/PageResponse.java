package com.parkmate.common.dto;

public record PageResponse<T>(java.util.List<T> items, long total, int page, int size) {}

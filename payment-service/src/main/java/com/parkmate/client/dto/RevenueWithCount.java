package com.parkmate.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for revenue data combined with session count
 * Used by internal endpoints to provide both revenue and count in a single response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueWithCount {
    private BigDecimal revenue;
    private Long count;
}
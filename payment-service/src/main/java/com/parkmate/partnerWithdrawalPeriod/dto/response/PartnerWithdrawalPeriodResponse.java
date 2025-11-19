package com.parkmate.partnerWithdrawalPeriod.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerWithdrawalPeriodResponse {
    private Long id;
    private Long lotId;
    private Long partnerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodEndDate;

    private String periodLabel;
    private Boolean isWithdrawn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime withdrawnAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private BigDecimal reservationRevenue;
    private BigDecimal subscriptionRevenue;
    private BigDecimal walkInRevenue;
    private BigDecimal grossRevenue;
    private BigDecimal platformFee;
    private BigDecimal netRevenue;
}

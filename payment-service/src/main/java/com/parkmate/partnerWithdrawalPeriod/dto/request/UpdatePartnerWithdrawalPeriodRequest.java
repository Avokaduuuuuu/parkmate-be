package com.parkmate.partnerWithdrawalPeriod.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePartnerWithdrawalPeriodRequest {
    private BigDecimal grossRevenue;
    private BigDecimal platformFee;
    private BigDecimal netRevenue;
    private BigDecimal walkInRevenue;
    private BigDecimal reservationRevenue;
    private BigDecimal subscriptionRevenue;
    private Integer totalSessions;
    private Integer walkInSessions;
    private Integer reservationSessions;
    private Integer subscriptionSessions;
}

package com.parkmate.partnerWithdrawalPeriod;

import lombok.Data;

@Data
public class PartnerWithdrawalPeriodFilterParams {
    private Long partnerId;
    private Long lotId;
    private Boolean isWithdrawn;
    private String periodLabel;
}

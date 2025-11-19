package com.parkmate.partnerWithdrawal;

import lombok.Data;

@Data
public class PartnerWithdrawalFilterParams {
    private Long partnerId;
    private Long lotId;
    private String status; // PROCESSING, COMPLETED, FAILED
}

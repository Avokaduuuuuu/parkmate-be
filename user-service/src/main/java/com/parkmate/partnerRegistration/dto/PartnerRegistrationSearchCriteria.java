package com.parkmate.partnerRegistration.dto;

import com.parkmate.common.enums.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerRegistrationSearchCriteria {

    // Company information
    private String companyName;
    private String taxNumber;
    private String businessLicenseNumber;
    private String companyAddress;
    private String companyEmail;
    private String companyPhone;

    // Contact person information
    private String contactPersonName;
    private String contactPersonEmail;

    // Status
    private RequestStatus status;

    // Submitted date range
    private LocalDateTime submittedAfter;
    private LocalDateTime submittedBefore;

    // Reviewed date range
    private LocalDateTime reviewedAfter;
    private LocalDateTime reviewedBefore;

    // Reviewer
    private Long reviewedBy;

    // Partner relationship
    private Boolean hasPartner;
}
package com.parkmate.partnerRegistration.dto;

import com.parkmate.common.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRegistrationSearchRequest {

    // Company information fields
    private String companyName;
    private String taxNumber;
    private String businessLicenseNumber;
    private String companyAddress;
    private String companyEmail;
    private String companyPhone;

    // Contact person fields
    private String contactPersonName;
    private String contactPersonEmail;

    // Status
    private RequestStatus status;

    // Date range filters - Submitted
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedBefore;

    // Date range filters - Reviewed
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedBefore;

    // Reviewer ID
    private Long reviewedBy;

    // Boolean filter
    private Boolean hasPartner;

    /**
     * Convert search request to criteria object
     */
    public PartnerRegistrationSearchCriteria toCriteria() {
        return PartnerRegistrationSearchCriteria.builder()
                .companyName(this.companyName)
                .taxNumber(this.taxNumber)
                .businessLicenseNumber(this.businessLicenseNumber)
                .companyAddress(this.companyAddress)
                .companyEmail(this.companyEmail)
                .companyPhone(this.companyPhone)
                .contactPersonName(this.contactPersonName)
                .contactPersonEmail(this.contactPersonEmail)
                .status(this.status)
                .submittedAfter(this.submittedAfter)
                .submittedBefore(this.submittedBefore)
                .reviewedAfter(this.reviewedAfter)
                .reviewedBefore(this.reviewedBefore)
                .reviewedBy(this.reviewedBy)
                .hasPartner(this.hasPartner)
                .build();
    }
}
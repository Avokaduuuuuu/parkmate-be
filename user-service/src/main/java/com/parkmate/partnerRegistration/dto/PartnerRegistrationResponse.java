package com.parkmate.partnerRegistration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.parkmate.common.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Partner registration response")
public class PartnerRegistrationResponse {

    @Schema(description = "Request unique identifier", example = "1")
    private Long id;

    @Schema(description = "Company name", example = "ABC Parking Solutions Ltd.")
    private String companyName;

    @Schema(description = "Tax identification number", example = "0123456789")
    private String taxNumber;

    @Schema(description = "Business license number", example = "BL-2024-001234")
    private String businessLicenseNumber;

    @Schema(description = "URL to business license file", example = "https://storage.parkmate.com/licenses/abc-license.pdf")
    private String businessLicenseFileUrl;

    @Schema(description = "Complete company address", example = "123 Nguyen Hue Street, District 1, Ho Chi Minh City")
    private String companyAddress;

    @Schema(description = "Company contact phone", example = "+84-28-1234-5678")
    private String companyPhone;

    @Schema(description = "Company contact email", example = "contact@abcparking.com")
    private String companyEmail;

    @Schema(description = "Description of business operations", example = "Providing smart parking solutions for commercial buildings")
    private String businessDescription;

    @Schema(description = "Contact person full name", example = "Nguyen Van A")
    private String contactPersonName;

    @Schema(description = "Contact person phone number", example = "+84-901-234-567")
    private String contactPersonPhone;

    @Schema(description = "Contact person email", example = "nguyen.vana@abcparking.com")
    private String contactPersonEmail;

    @Schema(description = "Request status", example = "PENDING")
    private RequestStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Request submission timestamp", example = "2024-09-23 10:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "ID of admin who reviewed this request", example = "456")
    private Long reviewedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Request review timestamp", example = "2024-09-23 14:30:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "Notes from admin during approval", example = "All documents verified successfully")
    private String approvalNotes;

    @Schema(description = "Reason for rejection if status is REJECTED", example = "Business license is expired")
    private String rejectionReason;

    @Schema(description = "Partner ID created from this request (if approved)", example = "789")
    private Long partnerId;

    @Schema(description = "ID of reviewer account", example = "456")
    private Long reviewerId;

    @Schema(description = "ID of account that submitted this registration request", example = "123")
    private Long submittedByAccountId;
}
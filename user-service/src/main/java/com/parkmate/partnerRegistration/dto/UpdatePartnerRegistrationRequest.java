package com.parkmate.partnerRegistration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parkmate.common.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update partner registration")
public class UpdatePartnerRegistrationRequest {

    // Status and review fields
    @Schema(description = "New status for the registration", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private RequestStatus status;

    @Schema(description = "Notes from admin during approval", example = "All documents verified successfully")
    private String approvalNotes;

    @Schema(description = "Reason for rejection (required if status is REJECTED)", example = "Business license is expired")
    private String rejectionReason;

    @Schema(description = "Id of admin approve/reject this registration request", example = "2")
    private Long reviewerId;

    // Company information fields
    @Schema(description = "Company name", example = "ABC Parking Solutions Ltd.")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;

    @Schema(description = "Tax identification number", example = "0123456789")
    @Size(max = 50, message = "Tax number must not exceed 50 characters")
    private String taxNumber;

    @Schema(description = "Business license number", example = "BL-2024-001234")
    @Size(max = 100, message = "Business license number must not exceed 100 characters")
    private String businessLicenseNumber;

    @Schema(description = "URL to business license file", example = "https://storage.parkmate.com/licenses/abc-license.pdf")
    @Size(max = 500, message = "Business license file URL must not exceed 500 characters")
    private String businessLicenseFileUrl;

    @Schema(description = "Complete company address", example = "123 Nguyen Hue Street, District 1, Ho Chi Minh City")
    private String companyAddress;

    @Schema(description = "Company contact phone", example = "+84-28-1234-5678")
    @Size(max = 20, message = "Company phone must not exceed 20 characters")
    private String companyPhone;

    @Schema(description = "Company contact email", example = "contact@abcparking.com")
    @Email(message = "Company email must be a valid email address")
    private String companyEmail;

    @Schema(description = "Description of business operations", example = "Providing smart parking solutions for commercial buildings")
    private String businessDescription;

    // Contact person fields
    @Schema(description = "Contact person full name", example = "Nguyen Van A")
    @Size(min = 2, max = 255, message = "Contact person name must be between 2 and 255 characters")
    private String contactPersonName;

    @Schema(description = "Contact person phone number", example = "+84-901-234-567")
    @Size(max = 20, message = "Contact person phone must not exceed 20 characters")
    private String contactPersonPhone;

    @Schema(description = "Contact person email", example = "nguyen.vana@abcparking.com")
    @Email(message = "Contact person email must be a valid email address")
    private String contactPersonEmail;

    // Custom validation method
    @JsonIgnore
    public boolean isValid() {
        if (RequestStatus.REJECTED.equals(status)) {
            return rejectionReason != null && !rejectionReason.trim().isEmpty();
        }
        return true;
    }
}
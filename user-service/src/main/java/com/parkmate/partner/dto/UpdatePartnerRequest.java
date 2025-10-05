package com.parkmate.partner.dto;

import com.parkmate.partner.PartnerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Update partner request - all fields are optional")
public record UpdatePartnerRequest(
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        @Schema(description = "Company name (max 255 chars)", example = "ABC Parking Solutions Ltd.")
        String companyName,

        @Size(max = 50, message = "Tax number must not exceed 50 characters")
        @Schema(description = "Tax identification number (max 50 chars)", example = "0123456789")
        String taxNumber,

        @Size(max = 100, message = "Business license number must not exceed 100 characters")
        @Schema(description = "Business license number (max 100 chars)", example = "BL-2024-001234")
        String businessLicenseNumber,

        @Size(max = 500, message = "Business license file URL must not exceed 500 characters")
        @Schema(description = "URL to business license file (max 500 chars)", example = "https://storage.parkmate.com/licenses/abc-license.pdf")
        String businessLicenseFileUrl,

        @Schema(description = "Complete company address", example = "123 Nguyen Hue Street, District 1, Ho Chi Minh City")
        String companyAddress,

        @Pattern(regexp = "^\\+?[0-9\\-\\s()]+$", message = "Invalid phone format")
        @Size(max = 20, message = "Company phone must not exceed 20 characters")
        @Schema(description = "Company contact phone (international format allowed)", example = "+84-28-1234-5678")
        String companyPhone,

        @Email(message = "Invalid email format")
        @Schema(description = "Company contact email (must be valid email)", example = "contact@abcparking.com")
        String companyEmail,

        @Schema(description = "Description of business operations", example = "Providing smart parking solutions for commercial buildings")
        String businessDescription,

        @Schema(description = "Partner status", example = "APPROVED", allowableValues = {"APPROVED", "SUSPENDED", "DELETED"})
        PartnerStatus status,

        @Schema(description = "Reason for suspension (required if status is SUSPENDED)", example = "Violation of terms and conditions")
        String suspensionReason
) {
}

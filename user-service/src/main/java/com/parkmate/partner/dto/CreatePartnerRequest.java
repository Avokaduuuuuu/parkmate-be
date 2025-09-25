package com.parkmate.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePartnerRequest(
        @Schema(description = "Reference to the original partner request", example = "123")
        Long approvalRequestId,

        @NotBlank
        @Schema(description = "Company name", example = "ABC Parking Solutions Ltd.")
        String companyName,

        @NotBlank
        @Size(max = 50)
        @Schema(description = "Tax identification number", example = "0123456789")
        String taxNumber,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "Business license number", example = "BL-2024-001234")
        String businessLicenseNumber,

        @Schema(description = "URL to business license file", example = "https://storage.parkmate.com/licenses/abc-license.pdf")
        String businessLicenseFileUrl,

        @NotBlank
        @Schema(description = "Complete company address", example = "123 Nguyen Hue Street, District 1, Ho Chi Minh City")
        String companyAddress,

        @Schema(description = "Company contact phone", example = "+84-28-1234-5678")
        String companyPhone,

        @Email
        @Schema(description = "Company contact email", example = "contact@abcparking.com")
        String companyEmail,

        @Schema(description = "Description of business operations", example = "Providing smart parking solutions for commercial buildings")
        String businessDescription
) {
}

package com.parkmate.partnerRegistration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new partner registration")
public class CreatePartnerRegistrationRequest {

    @NotBlank(message = "Tên doanh nghiệp không được để trống")
    @Size(min = 2, max = 200, message = "Tên doanh nghiệp phải từ 2-200 ký tự")
    @Schema(
            description = "Tên doanh nghiệp",
            example = "Coopmart Parking Services",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,     // ← Hiển thị trong Swagger
            maxLength = 200    // ← Hiển thị trong Swagger
    )
    private String companyName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "Account password (minimum 8 characters, max 100)", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Tax number is required")
    @Size(max = 50, message = "Tax number must not exceed 50 characters")
    @Schema(description = "Tax identification number (max 50 chars)", example = "0123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taxNumber;

    @NotBlank(message = "Business license number is required")
    @Size(max = 100, message = "Business license number must not exceed 100 characters")
    @Schema(description = "Business license number (max 100 chars)", example = "BL-2024-001234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessLicenseNumber;

    @Size(max = 500, message = "Business license file URL must not exceed 500 characters")
    @Schema(description = "URL to business license file (optional, max 500 chars)", example = "https://storage.parkmate.com/licenses/abc-license.pdf")
    private String businessLicenseFileUrl;

    @NotBlank(message = "Company address is required")
    @Schema(description = "Complete company address", example = "123 Nguyen Hue Street, District 1, Ho Chi Minh City", requiredMode = Schema.RequiredMode.REQUIRED)
    private String companyAddress;

    @Size(max = 20, message = "Company phone must not exceed 20 characters")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]+$", message = "Invalid phone format")
    @Schema(description = "Company contact phone (optional, max 20 chars, international format allowed)", example = "+84-28-1234-5678")
    private String companyPhone;

    @Email(message = "Invalid email format")
    @Schema(description = "Company contact email (optional, must be valid email)", example = "contact@abcparking.com")
    private String companyEmail;

    @Schema(description = "Description of business operations (optional)", example = "Providing smart parking solutions for commercial buildings")
    private String businessDescription;

    @NotBlank(message = "Contact person name is required")
    @Size(max = 255, message = "Contact person name must not exceed 255 characters")
    @Schema(description = "Contact person full name (max 255 chars)", example = "Nguyen Van A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPersonName;

    @NotBlank(message = "Contact person phone is required")
    @Size(max = 20, message = "Contact person phone must not exceed 20 characters")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]+$", message = "Invalid phone format")
    @Schema(description = "Contact person phone number (max 20 chars, international format allowed)", example = "+84-901-234-567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPersonPhone;

    @NotBlank(message = "Contact person email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Contact person email (must be valid email)", example = "nguyen.vana@abcparking.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPersonEmail;
}
package com.parkmate.partner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportPartnerDTO {
    
    private int rowNumber; // Để track row nào bị lỗi
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Tax number is required")
    @Pattern(regexp = "^[0-9]{10,13}$", message = "Tax number must be 10-13 digits")
    private String taxNumber;
    
    @NotBlank(message = "Business license number is required")
    private String businessLicenseNumber;
    
    private String businessLicenseFileUrl;
    
    @NotBlank(message = "Company address is required")
    private String companyAddress;
    
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Invalid phone format")
    private String companyPhone;
    
    @Email(message = "Invalid email format")
    private String companyEmail;
    
    private String businessDescription;
}
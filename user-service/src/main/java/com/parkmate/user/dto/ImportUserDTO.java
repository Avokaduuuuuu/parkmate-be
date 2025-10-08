package com.parkmate.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportUserDTO {

    private int rowNumber;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Phone must be 10-12 digits")
    private String phone;

    private String firstName;

    private String lastName;

    private String fullName;

    private String dateOfBirth; // Will be parsed to LocalDate

    private String address;

    private String idNumber;

    private String issuePlace;

    private String issueDate; // Will be parsed to LocalDate

    private String expiryDate; // Will be parsed to LocalDate
}

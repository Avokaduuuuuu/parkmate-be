package com.parkmate.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportVehicleDTO {

    private int rowNumber;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "User phone is required")
    private String userPhone;

    private String vehicleBrand;

    private String vehicleModel;

    private String vehicleColor;

    private String isActive;

    private String isElectric;
}

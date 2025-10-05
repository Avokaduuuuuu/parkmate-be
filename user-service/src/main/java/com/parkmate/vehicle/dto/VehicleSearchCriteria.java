package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "VehicleSearchCriteria",
        description = "Search criteria for filtering vehicles with various filter options"
)
public class VehicleSearchCriteria {

    // Single values
    @Schema(
            description = "Filter by specific user ID",
            example = "123",
            nullable = true
    )
    private Long userId;

    @Schema(
            description = "Filter by vehicle type",
            example = "CAR_4_SEATS",
            allowableValues = {"BIKE", "MOTORBIKE", "CAR_UP_TO_9_SEATS", "OTHER"},
            nullable = true
    )
    private VehicleType vehicleType;

    // Other fields
    @Schema(
            description = "Filter by license plate (partial match supported)",
            example = "51A-123",
            nullable = true
    )
    private String licensePlate;

    @Schema(
            description = "Filter by vehicle brand (partial match supported)",
            example = "Toyota",
            nullable = true
    )
    private String vehicleBrand;

    @Schema(
            description = "Filter by vehicle model (partial match supported)",
            example = "Camry",
            nullable = true
    )
    private String vehicleModel;

    @Schema(
            description = "Filter by vehicle color (partial match supported)",
            example = "White",
            nullable = true
    )
    private String vehicleColor;

    @Schema(
            description = "Filter by active status",
            example = "true",
            nullable = true
    )
    private Boolean isActive;

    @Schema(
            description = "Filter by electric vehicle status",
            example = "true",
            nullable = true
    )
    private Boolean isElectric;

    @Schema(
            description = "Filter vehicles created after this date",
            example = "2024-01-01T00:00:00",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime createdAfter;

    @Schema(
            description = "Filter vehicles created before this date",
            example = "2024-12-31T23:59:59",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime createdBefore;

    @Schema(
            description = "Filter vehicles updated after this date",
            example = "2024-01-01T00:00:00",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime updatedAfter;

    @Schema(
            description = "Filter vehicles updated before this date",
            example = "2024-12-31T23:59:59",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime updatedBefore;
}

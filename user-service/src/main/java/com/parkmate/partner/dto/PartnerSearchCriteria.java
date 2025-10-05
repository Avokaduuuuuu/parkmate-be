package com.parkmate.partner.dto;

import com.parkmate.partner.PartnerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PartnerSearchCriteria",
        description = "Search criteria for filtering partners with various filter options"
)
public class PartnerSearchCriteria {

    @Schema(description = "Filter by specific partner ID", example = "1", nullable = true)
    private Long partnerId;

    @Schema(
            description = "Filter by partner status",
            example = "APPROVED",
            allowableValues = {"APPROVED", "SUSPENDED", "DELETED"},
            nullable = true
    )
    private PartnerStatus status;

    @Schema(description = "Filter by multiple partner IDs", example = "[1, 2, 3]", nullable = true)
    private List<Long> partnerIds;

    @Schema(
            description = "Filter by multiple statuses",
            example = "[\"APPROVED\", \"SUSPENDED\"]",
            allowableValues = {"APPROVED", "SUSPENDED", "DELETED"},
            nullable = true
    )
    private List<PartnerStatus> statusList;

    @Schema(description = "Filter by company name (partial match supported)", example = "ABC Parking", nullable = true)
    private String companyName;

    @Schema(description = "Filter by tax number (exact match)", example = "0123456789", nullable = true)
    private String taxNumber;

    @Schema(description = "Filter by business license number (exact match)", example = "BL-2024-001234", nullable = true)
    private String businessLicenseNumber;

    @Schema(description = "Filter by company phone", example = "+84-28-1234-5678", nullable = true)
    private String companyPhone;

    @Schema(description = "Filter by company email", example = "contact@abcparking.com", nullable = true)
    private String companyEmail;

    @Schema(description = "Filter by company address (partial match supported)", example = "Nguyen Hue Street", nullable = true)
    private String companyAddress;

    @Schema(
            description = "Filter partners created after this date",
            example = "2024-01-01T00:00:00",
            type = "string", format = "date-time", nullable = true
    )
    private LocalDateTime createdAfter;

    @Schema(
            description = "Filter partners created before this date",
            example = "2024-12-31T23:59:59",
            type = "string", format = "date-time", nullable = true
    )
    private LocalDateTime createdBefore;

    @Schema(
            description = "Filter partners updated after this date",
            example = "2024-06-01T00:00:00",
            type = "string", format = "date-time", nullable = true
    )
    private LocalDateTime updatedAfter;

    @Schema(
            description = "Filter partners updated before this date",
            example = "2024-06-30T23:59:59",
            type = "string", format = "date-time", nullable = true
    )
    private LocalDateTime updatedBefore;
}

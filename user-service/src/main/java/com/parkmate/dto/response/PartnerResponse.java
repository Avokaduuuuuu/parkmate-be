package com.parkmate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.entity.enums.PartnerStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PartnerResponse(
        @Schema(description = "Partner unique identifier", example = "1")
        Long id,
        String companyName,
        String taxNumber,
        String businessLicenseNumber,
        String businessLicenseFileUrl,
        String companyAddress,
        String companyPhone,
        String companyEmail,
        String businessDescription,

        PartnerStatus status,
        String suspensionReason,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,

        @Schema(description = "List of associated account IDs")
        List<AccountBasicResponse> accounts
) {
}

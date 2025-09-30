package com.parkmate.partnerRegistration.dto;

import com.parkmate.common.enums.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update partner registration (review/approval)")
public class UpdatePartnerRegistrationRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New status for the registration", example = "APPROVED", required = true, allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private RequestStatus status;

    @Schema(description = "Notes from admin during approval", example = "All documents verified successfully")
    private String approvalNotes;

    @Schema(description = "Reason for rejection (required if status is REJECTED)", example = "Business license is expired")
    private String rejectionReason;

    // Custom validation method
    public boolean isValid() {
        if (RequestStatus.REJECTED.equals(status)) {
            return rejectionReason != null && !rejectionReason.trim().isEmpty();
        }
        return true;
    }
}
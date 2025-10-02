package com.parkmate.admin;

import com.parkmate.admin.dto.PartnerRegistrationApproveRequest;
import com.parkmate.admin.dto.PartnerRegistrationRejectRequest;
import com.parkmate.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-service/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/{reviewerId}/partner-registration/{partnerRegistrationId}/approve")
    public ResponseEntity<ApiResponse<String>> approvePartnerRegistration(
            @PathVariable Long reviewerId,
            @PathVariable Long partnerRegistrationId,
            @Valid @RequestBody PartnerRegistrationApproveRequest request) {

        adminService.approvePartnerRegistration(reviewerId, partnerRegistrationId, request);
        return ResponseEntity.ok(ApiResponse.success("Partner registration approved successfully"));
    }

    @PutMapping("/{reviewerId}/partner-registration/{partnerRegistrationId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectPartnerRegistration(
            @PathVariable Long reviewerId,
            @PathVariable Long partnerRegistrationId,
            @Valid @RequestBody PartnerRegistrationRejectRequest request) {

        adminService.rejectPartnerRegistration(reviewerId, partnerRegistrationId, request);
        return ResponseEntity.ok(ApiResponse.success("Partner registration rejected"));

    }
}

package com.parkmate.partnerRegistration;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.partnerRegistration.dto.CreatePartnerRegistrationRequest;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationResponse;
import com.parkmate.partnerRegistration.dto.PartnerRegistrationSearchRequest;
import com.parkmate.partnerRegistration.dto.UpdatePartnerRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/partner-registrations")
@Tag(name = "Partner Registration", description = "Endpoints for managing partner registrations")
public class PartnerRegistrationController {

    private final PartnerRegistrationService partnerRegistrationService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Partner Registration by ID",
            description = "Fetches the details of a partner registration by its ID."
    )
    public ResponseEntity<ApiResponse<PartnerRegistrationResponse>> findById(@PathVariable Long id) {
        PartnerRegistrationResponse response = partnerRegistrationService.getPartnerRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.success("Partner registration fetched successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Get All Partner Registration",
            description = "Fetches the details of all partner registration"
    )
    public ResponseEntity<ApiResponse<Page<PartnerRegistrationResponse>>> findAllPartnerRegistrations(
            @ModelAttribute PartnerRegistrationSearchRequest pageRequest,
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PartnerRegistrationResponse> partnerRegistrationResponsePage = partnerRegistrationService.getPartnerRegistrations(pageRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success("Partner registrations fetched successfully", partnerRegistrationResponsePage));
    }

    @PostMapping
    @Operation(
            summary = "Register a new Partner",
            description = "Creates a new partner registration request."
    )
    public ResponseEntity<ApiResponse<PartnerRegistrationResponse>> registerPartner(@RequestBody CreatePartnerRegistrationRequest request) {
        PartnerRegistrationResponse response = partnerRegistrationService.registerPartner(request);
        return ResponseEntity.ok(ApiResponse.success("Partner registration created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Partner Registration",
            description = "Updates the status and details of an existing partner registration."
    )
    public ResponseEntity<ApiResponse<PartnerRegistrationResponse>> updatePartnerRegistration(
            @PathVariable Long id, @RequestBody UpdatePartnerRegistrationRequest request) {
        PartnerRegistrationResponse response = partnerRegistrationService.updatePartnerRegistration(id, request);
        return ResponseEntity.ok(ApiResponse.success("Partner registration updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Partner Registration",
            description = "Deletes a partner registration by its ID."
    )
    public ResponseEntity<ApiResponse<Void>> deletePartnerRegistration(@PathVariable Long id) {
        partnerRegistrationService.deletePartnerRegistration(id);
        return ResponseEntity.ok(ApiResponse.success("Partner registration deleted successfully", null));
    }

}

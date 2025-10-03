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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-service/partner-registrations")
@Tag(name = "Partner Registration", description = "Endpoints for managing partner registrations")
public class PartnerRegistrationController {

    private final PartnerRegistrationService partnerRegistrationService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Partner Registration by ID",
            description = """
                    Retrieves detailed information of a partner registration request.
                    
                    **Parameters:**
                    - `id` (path): Partner registration ID (Long)
                    
                    **Returns:** Partner registration details including company info, status, and review information
                    """
    )
    public ResponseEntity<ApiResponse<PartnerRegistrationResponse>> findById(@PathVariable Long id) {
        PartnerRegistrationResponse response = partnerRegistrationService.getPartnerRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.success("Partner registration fetched successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "Get All Partner Registrations with Search and Pagination",
            description = """
                    Search and retrieve partner registration requests with pagination support.
                    
                    **Query Parameters:**
                    - `status` (optional): Filter by request status (PENDING, APPROVED, REJECTED)
                    - `companyName` (optional): Search by company name (partial match)
                    - `taxNumber` (optional): Search by tax number
                    - `page` (optional): Page number (default: 0)
                    - `size` (optional): Page size (default: 10)
                    - `sort` (optional): Sort field and direction (default: submittedAt,desc)
                    
                    **Returns:** Paginated list of partner registrations
                    """
    )
    public ResponseEntity<ApiResponse<Page<PartnerRegistrationResponse>>> findAllPartnerRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @ModelAttribute PartnerRegistrationSearchRequest pageRequest
    ) {
        Page<PartnerRegistrationResponse> partnerRegistrationResponsePage = partnerRegistrationService.getPartnerRegistrations(pageRequest, page, size, sortBy, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("Partner registrations fetched successfully", partnerRegistrationResponsePage));
    }

    @PostMapping
    @Operation(
            summary = "Register a new Partner",
            description = """
                    Submit a new partner registration request. A verification email will be sent to the contact person.
                    
                    **Request Body (CreatePartnerRegistrationRequest):**
                    - `companyName` (required): Company/business name
                    - `taxNumber` (required): Tax identification number (unique)
                    - `businessLicenseNumber` (required): Business license number
                    - `businessLicenseFileUrl` (optional): URL to uploaded business license
                    - `companyAddress` (required): Complete company address
                    - `companyPhone` (optional): Company contact phone
                    - `companyEmail` (optional): Company contact email
                    - `businessDescription` (optional): Description of business operations
                    - `contactPersonName` (required): Contact person full name
                    - `contactPersonPhone` (required): Contact person phone number
                    - `contactPersonEmail` (required): Contact person email (unique)
                    - `password` (required): Account password
                    
                    **Returns:** Created partner registration with PENDING status
                    """
    )
    public ResponseEntity<ApiResponse<PartnerRegistrationResponse>> registerPartner(@RequestBody CreatePartnerRegistrationRequest request) {
        PartnerRegistrationResponse response = partnerRegistrationService.registerPartner(request);
        return ResponseEntity.ok(ApiResponse.success("Partner registration created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Partner Registration Status (Admin Review)",
            description = """
                    Admin endpoint to approve or reject a partner registration request.
                    
                    **Parameters:**
                    - `id` (path): Partner registration ID to update
                    
                    **Request Body (UpdatePartnerRegistrationRequest):**
                    - `status` (required): New status - APPROVED or REJECTED
                    - `reviewerId` (required): Admin account ID performing the review
                    - `approvalNotes` (optional): Admin notes when approving
                    - `rejectionReason` (required if status=REJECTED): Reason for rejection
                    
                    **Business Logic:**
                    - If APPROVED: Creates Partner entity, sets account status to ACTIVE
                    - If REJECTED: Only updates status and rejection reason
                    
                    **Returns:** Updated partner registration with review information
                    """
    )
    public ResponseEntity<ApiResponse<PartnerRegistrationResponse>> updatePartnerRegistration(
            @PathVariable Long id, @RequestBody UpdatePartnerRegistrationRequest request) {
        PartnerRegistrationResponse response = partnerRegistrationService.updatePartnerRegistration(id, request);
        return ResponseEntity.ok(ApiResponse.success("Partner registration updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Partner Registration",
            description = """
                    Soft delete a partner registration by setting its status to REJECTED.
                    
                    **Parameters:**
                    - `id` (path): Partner registration ID to delete
                    
                    **Returns:** Success message
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deletePartnerRegistration(@PathVariable Long id) {
        partnerRegistrationService.deletePartnerRegistration(id);
        return ResponseEntity.ok(ApiResponse.success("Partner registration deleted successfully"));
    }

}

package com.parkmate.partner;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.partner.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partner Management", description = "APIs for managing parking lot partners")
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    @Operation(
            summary = "Search Partners with Pagination",
            description = """
                    Search and retrieve approved partners with pagination and filtering.

                    **Search Criteria (PartnerSearchRequest):**
                    - `companyName` (optional): Search by company name (partial match)
                    - `taxNumber` (optional): Search by tax number
                    - `status` (optional): Filter by partner status
                    - `companyEmail` (optional): Search by company email

                    **Returns:** Paginated list of partners
                    """
    )
    public ResponseEntity<ApiResponse<Page<PartnerResponse>>> getPartners(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortOrder,

            @ModelAttribute PartnerSearchRequest request) {

        PartnerSearchCriteria criteria = request.toCriteria();
        Page<PartnerResponse> result = partnerService.search(criteria, page, size, sortBy, sortOrder);

        return ResponseEntity.ok(ApiResponse.<Page<PartnerResponse>>builder()
                .success(true)
                .message("Partner searched successfully")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Partner by ID",
            description = "Retrieve detailed information about a specific partner."
    )
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartner(
            @Parameter(description = "Partner ID", example = "1")
            @PathVariable long id) {
        PartnerResponse response = partnerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Partner fetched successfully", response));
    }

    @PostMapping
    @Operation(
            summary = "Create Partner (Admin Only)",
            description = """
                    Manually create a new partner entity (typically done through registration approval process).

                    **Request Body:** CreatePartnerRequest with company details
                    """
    )
    public ResponseEntity<ApiResponse<PartnerResponse>> add(@RequestBody CreatePartnerRequest req) {
        PartnerResponse mobileDeviceResponse = partnerService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Partner created successfully", mobileDeviceResponse));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update Partner",
            description = "Update existing partner information."
    )
    public ResponseEntity<ApiResponse<PartnerResponse>> update(
            @Parameter(description = "Partner ID to update", example = "1")
            @PathVariable long id,
            @RequestBody UpdatePartnerRequest req) {
        PartnerResponse partnerResponse = partnerService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Partner updated successfully", partnerResponse));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Partner",
            description = "Soft delete a partner by changing their status."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Partner ID to delete", example = "1")
            @PathVariable long id) {
        partnerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Partner deleted successfully"));
    }

}

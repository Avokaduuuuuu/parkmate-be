package com.parkmate.controller;

import com.parkmate.dto.criteria.PartnerSearchCriteria;
import com.parkmate.dto.request.CreatePartnerRequest;
import com.parkmate.dto.request.PartnerSearchRequest;
import com.parkmate.dto.request.UpdatePartnerRequest;
import com.parkmate.dto.response.ApiResponse;
import com.parkmate.dto.response.PartnerResponse;
import com.parkmate.service.PartnerService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user-service/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    @Schema(description = "Search Partners with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<PartnerResponse>>> getPartners(
            @ModelAttribute PartnerSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        PartnerSearchCriteria criteria = request.toCriteria();
        Page<PartnerResponse> result = partnerService.search(criteria, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PartnerResponse>>builder()
                .success(true)
                .message("Partner searched successfully")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{id}")
    @Schema(description = "Get Partner details by ID")
    public ResponseEntity<ApiResponse<PartnerResponse>> getPartner(@PathVariable long id) {
        PartnerResponse response = partnerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Partner fetched successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PartnerResponse>> add(@RequestBody CreatePartnerRequest req) {
        PartnerResponse mobileDeviceResponse = partnerService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Partner created successfully", mobileDeviceResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> update(
            @PathVariable long id,
            @RequestBody UpdatePartnerRequest req) {
        PartnerResponse partnerResponse = partnerService.update(id, req);
        return ResponseEntity.ok(ApiResponse.success("Partner updated successfully", partnerResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable long id) {
        partnerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Partner deleted successfully", null));
    }

}

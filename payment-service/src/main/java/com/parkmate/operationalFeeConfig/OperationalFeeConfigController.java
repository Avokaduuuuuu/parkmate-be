package com.parkmate.operationalFeeConfig;

import com.parkmate.common.ApiResponse;
import com.parkmate.operationalFeeConfig.dto.req.OperationalFeeConfigCreateRequest;
import com.parkmate.operationalFeeConfig.dto.req.OperationalFeeConfigUpdateRequest;
import com.parkmate.operationalFeeConfig.dto.resp.OperationalFeeConfigResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/operational-fee-config")
@RequiredArgsConstructor
@Tag(name = "Operational Fee Config", description = "Operational fee configuration management APIs")
public class OperationalFeeConfigController {
    private final OperationalFeeConfigService operationalFeeConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OperationalFeeConfigResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,
            OperationalFeeConfigFilterParams filterParams
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetching all operational fee config successfully",
                                operationalFeeConfigService.findAll(page, size, sortBy, sortOrder, filterParams)
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OperationalFeeConfigResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetching operational fee config by id successfully",
                                operationalFeeConfigService.findById(id)
                        )
                );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OperationalFeeConfigResponse>> create(
            @Valid @RequestBody OperationalFeeConfigCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Creating operational fee config successfully",
                                operationalFeeConfigService.addOperationalFeeConfig(request)
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OperationalFeeConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OperationalFeeConfigUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Updating operational fee config successfully",
                                operationalFeeConfigService.updateOperationalFeeConfig(id, request)
                        )
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OperationalFeeConfigResponse>> delete(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Deleting operational fee config successfully",
                                operationalFeeConfigService.deleteOperationalFeeConfig(id)
                        )
                );
    }
}

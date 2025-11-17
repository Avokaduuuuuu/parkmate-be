package com.parkmate.operational_fee_config;

import com.parkmate.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/operational-fee-config")
@RequiredArgsConstructor
public class OperationalFeeConfigController {
    private final OperationalFeeConfigService operationalFeeConfigService;

    @GetMapping
    public ResponseEntity<?> findAll(
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
    public ResponseEntity<?> findById(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                "Fetching operational fee config by id successfully",
                                operationalFeeConfigService.findById(id)
                        )
                );
    }

}

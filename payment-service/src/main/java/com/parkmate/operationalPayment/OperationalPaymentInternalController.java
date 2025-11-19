package com.parkmate.operationalPayment;

import com.parkmate.common.ApiResponse;
import com.parkmate.operationalPayment.dto.CreateOperationalPaymentRequest;
import com.parkmate.operationalPayment.dto.OperationalPaymentResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/operational-payments")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class OperationalPaymentInternalController {

    private final OperationalPaymentService operationalPaymentService;

    /**
     * Internal endpoint for parking-lot-service to create operational payments
     * Does not require X-User-Id header as partnerId is included in the request
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OperationalPaymentResponse>> createOperationalPayment(
            @Valid @RequestBody CreateOperationalPaymentRequest request) {

        log.info("Internal: Creating operational payment - lotId: {}, partnerId: {}, area: {} sqm",
                request.lotId(), request.partnerId(), request.lotAreaSqm());

        OperationalPaymentResponse response = operationalPaymentService.createPaymentForParkingLot(request);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Operational payment created successfully. Please complete payment to activate parking lot."
        ));
    }
}
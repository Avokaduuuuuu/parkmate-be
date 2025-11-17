package com.parkmate.operationalPayment;

import com.parkmate.common.ApiResponse;
import com.parkmate.operationalPayment.dto.CreateOperationalPaymentRequest;
import com.parkmate.operationalPayment.dto.OperationalPaymentResponse;
import com.parkmate.operationalPayment.dto.OperationalPaymentStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/operational-payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operational Payment Management", description = "Endpoints for parking lot operational fee payments")
public class OperationalPaymentController {

    private final OperationalPaymentService operationalPaymentService;

    @PostMapping
    @Operation(
            summary = "Create operational fee payment",
            description = """
                    Creates a new operational fee payment for parking lot activation.
                    
                    **Process:**
                    1. Fetches active operational fee configuration
                    2. Calculates total fee: lotAreaSqm × pricePerSqm × billingPeriodMonths
                    3. Creates payment record with PENDING status
                    4. Generates PayOS payment link (QR code + bank transfer)
                    5. Returns payment link and details
                    
                    **Payment Flow:**
                    - Partner receives payment link and QR code
                    - Partner completes payment via bank transfer or QR scan
                    - PayOS webhook confirms payment
                    - Parking lot is automatically activated to ACTIVE status
                    
                    **Fee Calculation Example:**
                    - Lot area: 500 sqm
                    - Price per sqm: 50,000 VND
                    - Billing period: 6 months
                    - Total fee: 500 × 50,000 × 6 = 150,000,000 VND
                    
                    **Note:** Payment must be completed within 30 days of the due date.
                    """
    )
    public ResponseEntity<ApiResponse<OperationalPaymentResponse>> createOperationalPayment(
            @Valid @RequestBody CreateOperationalPaymentRequest request,
            @RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userIdHeader) {

        log.info("Creating operational payment - lotId: {}, partnerId: {}, area: {} sqm",
                request.lotId(), request.partnerId(), request.lotAreaSqm());

        OperationalPaymentResponse response = operationalPaymentService.createPaymentForParkingLot(request);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Operational payment created successfully. Please complete payment to activate parking lot."
        ));
    }

    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment status",
            description = """
                    Retrieves the current status of an operational payment.
                    
                    **Payment Statuses:**
                    - PENDING: Payment link created, awaiting payment
                    - PAID: Payment completed successfully, lot activated
                    - OVERDUE: Payment not completed before due date
                    - CANCELLED: Payment cancelled by user or system
                    """
    )
    public ResponseEntity<ApiResponse<OperationalPaymentStatusResponse>> getPaymentStatus(
            @PathVariable Long paymentId) {

        log.info("Getting payment status for operational payment: {}", paymentId);

        OperationalPaymentStatusResponse response = operationalPaymentService.getPaymentStatus(paymentId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(
            summary = "Cancel payment",
            description = """
                    Cancels a pending operational payment.
                    
                    **Requirements:**
                    - Payment must be in PENDING status
                    - Cannot cancel already paid or cancelled payments
                    
                    **Effect:**
                    - Payment status changes to CANCELLED
                    - Parking lot remains in PENDING_PAYMENT status
                    - New payment must be created to activate lot
                    """
    )
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userIdHeader) {

        log.info("Cancelling operational payment: {}", paymentId);

        operationalPaymentService.cancelPayment(paymentId);

        return ResponseEntity.ok(ApiResponse.success(
                "Payment cancelled successfully"
        ));
    }
}
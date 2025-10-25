package com.parkmate.payos;

import com.parkmate.common.ApiResponse;
import com.parkmate.payos.dto.PaymentCancelResponse;
import com.parkmate.payos.dto.PaymentStatusResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

@RestController
@RequestMapping("/api/v1/payment-service/payos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PayOS Payment", description = "PayOS payment integration endpoints")
public class PayOSController {

    private final PayOSService payOSService;

    /**
     * Create a new PayOS payment link for wallet top-up
     */
    @PostMapping("/payment")
    @Operation(summary = "Create PayOS payment link", description = "Creates a payment link for wallet top-up using PayOS")
    public ResponseEntity<ApiResponse<CreatePaymentLinkResponse>> createPayment(
            @RequestParam @Parameter(description = "Amount in VND (minimum 1000)", required = true) Long amount,
            @RequestHeader(value = "X-User-Id", required = false) @Parameter(hidden = true) String userIdHeader) {
        log.info("Creating PayOS payment for accountId: {}, amount: {}", userIdHeader, amount);
        CreatePaymentLinkResponse response = payOSService.createPayment(userIdHeader, amount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    /**
     * PayOS webhook callback endpoint
     * This endpoint receives payment status updates from PayOS
     */
    @PostMapping("/payos_transfer_handler")
    @Hidden
    @Operation(summary = "PayOS webhook callback", description = "Receives payment status notifications from PayOS")
    public ResponseEntity<ApiResponse<Boolean>> handleWebhook(
            @RequestBody String webhookBody,
            @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        log.info("Received PayOS webhook with signature: {}", signature);
        return ResponseEntity.ok(ApiResponse.success(payOSService.processWebhook(webhookBody, signature), "Webhook processed successfully"));
    }

    /**
     * PayOS return URL endpoint
     * This endpoint handles user redirect from PayOS payment page
     */
    @GetMapping("/return")
    @Operation(summary = "PayOS return URL", description = "Handles user redirect after payment completion")
    @Hidden
    public ResponseEntity<ApiResponse<String>> handleReturnUrl(
            @RequestParam @Parameter(description = "Order code") Long orderCode,
            @RequestParam @Parameter(description = "Payment status code") String code,
            @RequestParam(required = false) @Parameter(description = "Cancel flag") Boolean cancel,
            @RequestParam(required = false) @Parameter(description = "Payment status") String status) {

        log.info("PayOS return - orderCode: {}, code: {}, cancel: {}, status: {}",
                orderCode, code, cancel, status);

        if ("00".equals(code) && !Boolean.TRUE.equals(cancel)) {
            return ResponseEntity.ok(
                    ApiResponse.success("Payment completed successfully for order: " + orderCode)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.error("PAYMENT_FAILED", "Payment failed or cancelled for order: " + orderCode)
            );
        }
    }

    /**
     * PayOS cancel URL endpoint
     * This endpoint handles user cancellation from PayOS payment page
     */
    @GetMapping("/cancel")
    @Operation(summary = "PayOS cancel URL", description = "Handles user cancellation of payment")
    @Hidden
    public ResponseEntity<ApiResponse<String>> handleCancelUrl(
            @RequestParam @Parameter(description = "Order code") Long orderCode,
            @RequestParam(required = false) @Parameter(description = "Cancel reason") String reason) {

        log.info("PayOS payment cancelled - orderCode: {}, reason: {}", orderCode, reason);

        return ResponseEntity.ok(
                ApiResponse.error("PAYMENT_CANCELLED", "Payment cancelled for order: " + orderCode)
        );
    }

    @PostMapping("/cancel")
    @Operation(summary = "PayOS cancel URL", description = "Handles user cancellation of payment")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancelPayment(
            @RequestParam @Parameter(description = "Order code") Long orderCode,
            @RequestParam(required = false) @Parameter(description = "Cancel reason") String reason) {

        log.info("PayOS payment cancelled - orderCode: {}, reason: {}", orderCode, reason);
        return ResponseEntity.ok(ApiResponse.success(payOSService.cancelPayment(orderCode, reason)));
    }

    @GetMapping("/status/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> statusPayment(@PathVariable Long orderCode) {
        return ResponseEntity.ok(ApiResponse.success(payOSService.retrievePaymentStatus(orderCode)));
    }
}

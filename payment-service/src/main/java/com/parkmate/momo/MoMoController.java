package com.parkmate.momo;

import com.parkmate.momo.dto.MoMoIPNRequest;
import com.parkmate.momo.dto.MoMoIPNResponse;
import com.parkmate.momo.dto.MoMoPaymentResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/momo")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class MoMoController {

    private final MomoService momoService;

    @PostMapping("/create")
    public ResponseEntity<MoMoPaymentResponse> createPayment(
            @RequestParam Long userId,
            @RequestParam Long amount,
            @RequestParam(required = false) String orderInfo) {
        MoMoPaymentResponse response = momoService.create(userId, amount, orderInfo);
        return ResponseEntity.ok(response);
    }

    /**
     * MoMo IPN (Instant Payment Notification) callback endpoint
     * This endpoint receives payment status updates from MoMo
     */
    @PostMapping("/ipn")
    public ResponseEntity<MoMoIPNResponse> handleIPNCallback(@RequestBody MoMoIPNRequest request) {
        log.info("Received MoMo IPN callback for orderId: {}, transId: {}",
                request.getOrderId(), request.getTransId());

        boolean success = momoService.processIPNCallback(request);

        MoMoIPNResponse response;
        if (success) {
            response = MoMoIPNResponse.success(
                    request.getPartnerCode(),
                    request.getRequestId(),
                    request.getOrderId()
            );
        } else {
            response = MoMoIPNResponse.error(
                    request.getPartnerCode(),
                    request.getRequestId(),
                    request.getOrderId(),
                    "Failed to process payment"
            );
        }

        return ResponseEntity.ok(response);
    }

    /**
     * MoMo redirect callback endpoint
     * This endpoint handles user redirect from MoMo payment page
     */
    @GetMapping("/callback")
    public ResponseEntity<String> handleRedirectCallback(
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam Integer resultCode,
            @RequestParam(required = false) String message) {

        log.info("MoMo redirect callback - orderId: {}, resultCode: {}", orderId, resultCode);

        if (resultCode == 0) {
            return ResponseEntity.ok("Payment successful for order: " + orderId);
        } else {
            return ResponseEntity.ok("Payment failed for order: " + orderId + ". Message: " + message);
        }
    }
}

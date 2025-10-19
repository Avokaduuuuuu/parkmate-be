package com.parkmate.payos;


import com.parkmate.payos.dto.PaymentCancelResponse;
import com.parkmate.payos.dto.PaymentStatusResponse;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

public interface PayOSService {
    CreatePaymentLinkResponse createPayment(Long userId, Long amount, String description);

    void processWebhook(String webhookBody, String signature);

    PaymentCancelResponse cancelPayment(Long orderCode, String reason);

    PaymentStatusResponse retrievePaymentStatus(Long orderCode);
}
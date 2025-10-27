package com.parkmate.payos;


import com.parkmate.payos.dto.PaymentCancelResponse;
import com.parkmate.payos.dto.PaymentStatusResponse;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

public interface PayOSService {
    CreatePaymentLinkResponse createPayment(String userId, Long amount);

    Boolean processWebhook(String webhookBody, String signature);

    PaymentCancelResponse cancelPayment(Long orderCode);

    PaymentStatusResponse retrievePaymentStatus(Long orderCode);
}
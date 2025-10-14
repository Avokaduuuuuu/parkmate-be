package com.parkmate.payos;


import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

public interface PayOSService {
    CreatePaymentLinkResponse createPayment(Long userId, Long amount, String description);

    boolean processWebhook(String webhookBody, String signature);
}
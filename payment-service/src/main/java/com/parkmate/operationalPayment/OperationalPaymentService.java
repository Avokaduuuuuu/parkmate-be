package com.parkmate.operationalPayment;

import com.parkmate.operationalPayment.dto.CreateOperationalPaymentRequest;
import com.parkmate.operationalPayment.dto.OperationalPaymentResponse;
import com.parkmate.operationalPayment.dto.OperationalPaymentStatusResponse;

public interface OperationalPaymentService {

    OperationalPaymentResponse createPaymentForParkingLot(CreateOperationalPaymentRequest request);

    OperationalPaymentStatusResponse getPaymentStatus(Long paymentId);

    void confirmPayment(String paymentTransactionId);

    void cancelPayment(Long paymentId);
}
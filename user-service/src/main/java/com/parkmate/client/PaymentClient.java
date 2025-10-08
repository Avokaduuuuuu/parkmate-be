package com.parkmate.client;

import com.parkmate.client.dto.request.CreateWalletRequest;
import com.parkmate.client.dto.response.WalletResponse;
import com.parkmate.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/v1/payment-service/wallets")
    ApiResponse<WalletResponse> createPayment(@RequestBody CreateWalletRequest request);

}

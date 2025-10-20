package com.parkmate.momo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MoMoPaymentRequest {

    @NotBlank(message = "Partner code is required")
    @JsonProperty("partnerCode")
    private String partnerCode;

    @NotBlank(message = "Access key is required")
    @JsonProperty("accessKey")
    private String accessKey;

    @NotBlank(message = "Request ID is required")
    @JsonProperty("requestId")
    private String requestId;

    @NotNull(message = "Amount is required")
    @Min(value = 1000, message = "Minimum amount is 1000 VND")
    @JsonProperty("amount")
    private String amount;  // MoMo nhận String, không phải Long

    @NotBlank(message = "Order ID is required")
    @JsonProperty("orderId")
    private String orderId;

    @NotBlank(message = "Order info is required")
    @JsonProperty("orderInfo")
    private String orderInfo;

    @NotBlank(message = "Redirect URL is required")
    @JsonProperty("redirectUrl")
    private String redirectUrl;

    @NotBlank(message = "IPN URL is required")
    @JsonProperty("ipnUrl")
    private String ipnUrl;

    @NotBlank(message = "Request type is required")
    @JsonProperty("requestType")
    private String requestType;  // Default: "captureWallet"

    @JsonProperty("extraData")
    private String extraData;  // Optional, có thể empty string

    @JsonProperty("lang")
    private String lang;  // Default: "vi"

    @NotBlank(message = "Signature is required")
    @JsonProperty("signature")
    private String signature;

}

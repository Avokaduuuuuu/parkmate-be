package com.parkmate.momo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoMoIPNRequest {

    @JsonProperty("partnerCode")
    private String partnerCode;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("amount")
    private Long amount;  // Ở đây MoMo gửi Long, không phải String

    @JsonProperty("orderInfo")
    private String orderInfo;

    @JsonProperty("orderType")
    private String orderType;  // VD: "momo_wallet"

    @JsonProperty("transId")
    private Long transId;  // Transaction ID từ MoMo - LƯU VÀO external_transaction_id

    @JsonProperty("resultCode")
    private Integer resultCode;  // 0 = Success

    @JsonProperty("message")
    private String message;

    @JsonProperty("payType")
    private String payType;  // VD: "qr", "app", "web"

    @JsonProperty("responseTime")
    private Long responseTime;

    @JsonProperty("extraData")
    private String extraData;

    @JsonProperty("signature")
    private String signature;  // CẦN VERIFY signature này!

    /**
     * Helper method: Check xem payment có thành công không
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    /**
     * Helper method: Check xem payment có bị fail không
     */
    public boolean isFailed() {
        return resultCode != null && resultCode != 0;
    }

}

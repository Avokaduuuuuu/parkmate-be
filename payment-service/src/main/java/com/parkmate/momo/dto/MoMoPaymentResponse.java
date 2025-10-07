package com.parkmate.momo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoMoPaymentResponse {

    @JsonProperty("partnerCode")
    private String partnerCode;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("responseTime")
    private Long responseTime;  // Timestamp

    @JsonProperty("message")
    private String message;  // Success message hoặc error message

    @JsonProperty("resultCode")
    private Integer resultCode;  // 0 = Success, khác 0 = Failed

    @JsonProperty("payUrl")
    private String payUrl;  // URL để redirect user đến trang thanh toán MoMo

    @JsonProperty("deeplink")
    private String deeplink;  // Deep link để mở MoMo app

    @JsonProperty("qrCodeUrl")
    private String qrCodeUrl;  // URL QR code để quét

    /**
     * Helper method: Check xem payment request có thành công không
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }

    /**
     * Helper method: Lấy URL để redirect user
     * Priority: payUrl > deeplink > qrCodeUrl
     */
    public String getRedirectUrl() {
        if (payUrl != null && !payUrl.isEmpty()) {
            return payUrl;
        }
        if (deeplink != null && !deeplink.isEmpty()) {
            return deeplink;
        }
        return qrCodeUrl;
    }

}
